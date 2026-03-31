package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.domain.TenantRegistry;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryJpaRepository;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryMapper;
import com.phoenixtask.controlplane.application.audit.AuditDomain;
import com.phoenixtask.controlplane.application.audit.AuditLogService;
import com.phoenixtask.controlplane.application.audit.AuditOutcome;
import com.phoenixtask.shared.error.ProvisioningException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.shared.tenant.TenantCredentialMode;
import com.phoenixtask.shared.tenant.TenantLifecyclePolicy;
import com.phoenixtask.shared.tenant.TenantLifecycleStatus;
import com.phoenixtask.workspace.infrastructure.config.WorkspaceProperties;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class TenantProvisioningService {

  private static final Logger log = LoggerFactory.getLogger(TenantProvisioningService.class);
  private static final Set<String> ALLOWED_STATUS = TenantLifecycleStatus.allowedValues();
  private static final Set<String> ALLOWED_CREDENTIAL_MODES = TenantCredentialMode.allowedValues();
  private static final String DEFAULT_ADMIN_DB = "postgres";

  private final TenantRegistryJpaRepository repository;
  private final WorkspaceProperties workspaceProperties;
  private final AuditLogService auditLogService;
  private final ObservabilityMetrics metrics;

  public TenantProvisioningService(
      TenantRegistryJpaRepository repository,
      WorkspaceProperties workspaceProperties,
      AuditLogService auditLogService,
      ObservabilityMetrics metrics
  ) {
    this.repository = repository;
    this.workspaceProperties = workspaceProperties;
    this.auditLogService = auditLogService;
    this.metrics = metrics;
  }

  public TenantRegistry provisionTenant(TenantRegistry tenant) {
    try {
      validate(tenant);
    } catch (ValidationException ex) {
      auditLogService.recordSystemEvent(
          AuditDomain.TENANT,
          "TENANT_PROVISIONING_FAILED",
          tenant.getCode(),
          "TENANT",
          tenant.getCode(),
          AuditOutcome.FAILURE,
          "VALIDATION_ERROR: " + ex.getMessage()
      );
      metrics.recordProvisioningOutcome("validation_failed");
      throw ex;
    }
    TenantRegistry pending = saveWithStatus(tenant, "PENDING");
    boolean createdDatabase = false;
    auditLogService.recordSystemEvent(
        AuditDomain.TENANT,
        "TENANT_PROVISIONING_STARTED",
        pending.getCode(),
        "TENANT",
        pending.getCode(),
        AuditOutcome.SUCCESS,
        null
    );
    try {
      createdDatabase = ensureDatabase(pending);
      migrateWorkspaceSchema(pending);
      TenantRegistry active = updateStatus(pending.getCode(), "ACTIVE");
      auditLogService.recordSystemEvent(
          AuditDomain.TENANT,
          "TENANT_PROVISIONING_SUCCEEDED",
          pending.getCode(),
          "TENANT",
          pending.getCode(),
          AuditOutcome.SUCCESS,
          null
      );
      metrics.recordProvisioningOutcome("success");
      return active;
    } catch (Exception ex) {
      log.error("Tenant provisioning failed for {}: {}", pending.getCode(), ex.getMessage(), ex);
      updateStatus(pending.getCode(), "FAILED");
      if (createdDatabase) {
        attemptDropDatabase(pending);
      }
      auditLogService.recordSystemEvent(
          AuditDomain.TENANT,
          "TENANT_PROVISIONING_FAILED",
          pending.getCode(),
          "TENANT",
          pending.getCode(),
          AuditOutcome.FAILURE,
          ex.getClass().getSimpleName()
      );
      metrics.recordProvisioningOutcome("failure");
      throw new ProvisioningException("Tenant provisioning failed. Tenant marked as FAILED.");
    }
  }

  private void validate(TenantRegistry tenant) {
    if (repository.findByCode(tenant.getCode()).isPresent()) {
      throw new ValidationException("Tenant code already exists");
    }
    if (tenant.getDbPort() == null || tenant.getDbPort() <= 0) {
      throw new ValidationException("dbPort must be a positive integer");
    }
    if (tenant.getDbHost() == null || tenant.getDbHost().isBlank()) {
      throw new ValidationException("dbHost is required");
    }
    if (tenant.getDbName() == null || tenant.getDbName().isBlank()) {
      throw new ValidationException("dbName is required");
    }
    if (!isValidIdentifier(tenant.getDbName())) {
      throw new ValidationException("dbName must contain only letters, numbers, or underscore");
    }
    if (tenant.getDbSchema() != null && !tenant.getDbSchema().isBlank()
        && !isValidIdentifier(tenant.getDbSchema())) {
      throw new ValidationException("dbSchema must contain only letters, numbers, or underscore");
    }
    String expectedHost = workspaceProperties.getDatasource().getHost();
    Integer expectedPort = workspaceProperties.getDatasource().getPort();
    String expectedUser = workspaceProperties.getDatasource().getUsername();
    if (expectedHost != null && !expectedHost.equalsIgnoreCase(tenant.getDbHost())) {
      throw new ValidationException("dbHost must match workspace datasource host");
    }
    if (expectedPort != null && !expectedPort.equals(tenant.getDbPort())) {
      throw new ValidationException("dbPort must match workspace datasource port");
    }
    if (expectedUser != null && !expectedUser.equals(tenant.getDbUsername())) {
      throw new ValidationException("dbUsername must match workspace datasource username");
    }
    if (!ALLOWED_STATUS.contains(tenant.getStatus())) {
      throw new ValidationException(
          "status must be one of: " + String.join(", ", ALLOWED_STATUS)
      );
    }
    TenantLifecyclePolicy.parseOrThrow(tenant.getStatus());
    if (tenant.getCredentialMode() == null || tenant.getCredentialMode().isBlank()) {
      tenant.setCredentialMode(TenantCredentialMode.SHARED.name());
    }
    if (!ALLOWED_CREDENTIAL_MODES.contains(tenant.getCredentialMode())) {
      throw new ValidationException(
          "credentialMode must be one of: " + String.join(", ", ALLOWED_CREDENTIAL_MODES)
      );
    }
  }

  private TenantRegistry saveWithStatus(TenantRegistry tenant, String status) {
    tenant.setStatus(status);
    TenantRegistryEntity entity = TenantRegistryMapper.toEntity(tenant);
    TenantRegistryEntity saved = repository.save(entity);
    return TenantRegistryMapper.toDomain(saved);
  }

  @Transactional
  protected TenantRegistry updateStatus(String code, String status) {
    TenantRegistryEntity entity = repository.findByCode(code)
        .orElseThrow(() -> new ValidationException("Tenant not found"));
    entity.setStatus(status);
    TenantRegistryEntity saved = repository.save(entity);
    return TenantRegistryMapper.toDomain(saved);
  }

  private boolean ensureDatabase(TenantRegistry tenant) {
    JdbcTemplate admin = new JdbcTemplate(buildAdminDataSource(tenant));
    Boolean exists = admin.queryForObject(
        "SELECT EXISTS (SELECT 1 FROM pg_database WHERE datname = ?)",
        Boolean.class,
        tenant.getDbName()
    );
    if (Boolean.TRUE.equals(exists)) {
      return false;
    }
    log.info("Creating tenant database {}", tenant.getDbName());
    admin.execute("CREATE DATABASE \"" + tenant.getDbName() + "\"");
    return true;
  }

  private void migrateWorkspaceSchema(TenantRegistry tenant) {
    DataSource tenantDataSource = buildTenantDataSource(tenant);
    if (tenant.getDbSchema() != null && !tenant.getDbSchema().isBlank()) {
      JdbcTemplate template = new JdbcTemplate(tenantDataSource);
      template.execute("CREATE SCHEMA IF NOT EXISTS \"" + tenant.getDbSchema() + "\"");
    }
    var flywayConfig = Flyway.configure()
        .dataSource(tenantDataSource)
        .baselineOnMigrate(false)
        .locations(workspaceProperties.getFlyway().getLocations().toArray(new String[0]));
    if (tenant.getDbSchema() != null && !tenant.getDbSchema().isBlank()) {
      flywayConfig.schemas(tenant.getDbSchema()).defaultSchema(tenant.getDbSchema());
    }
    Flyway flyway = flywayConfig.load();
    flyway.migrate();
  }

  private void attemptDropDatabase(TenantRegistry tenant) {
    try {
      JdbcTemplate admin = new JdbcTemplate(buildAdminDataSource(tenant));
      admin.execute("DROP DATABASE IF EXISTS \"" + tenant.getDbName() + "\"");
    } catch (Exception ex) {
      log.warn("Failed to drop tenant database {} after provisioning failure", tenant.getDbName(), ex);
    }
  }

  private DataSource buildAdminDataSource(TenantRegistry tenant) {
    String url = String.format(
        "jdbc:postgresql://%s:%d/%s",
        tenant.getDbHost(),
        tenant.getDbPort(),
        DEFAULT_ADMIN_DB
    );
    return DataSourceBuilder.create()
        .driverClassName("org.postgresql.Driver")
        .url(url)
        .username(workspaceProperties.getDatasource().getUsername())
        .password(workspaceProperties.getDatasource().getPassword())
        .build();
  }

  private DataSource buildTenantDataSource(TenantRegistry tenant) {
    String url = String.format(
        "jdbc:postgresql://%s:%d/%s",
        tenant.getDbHost(),
        tenant.getDbPort(),
        tenant.getDbName()
    );
    return DataSourceBuilder.create()
        .driverClassName("org.postgresql.Driver")
        .url(url)
        .username(tenant.getDbUsername())
        .password(workspaceProperties.getDatasource().getPassword())
        .build();
  }

  private boolean isValidIdentifier(String value) {
    if (value == null || value.isBlank()) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (!(Character.isLetterOrDigit(ch) || ch == '_')) {
        return false;
      }
    }
    return true;
  }
}
