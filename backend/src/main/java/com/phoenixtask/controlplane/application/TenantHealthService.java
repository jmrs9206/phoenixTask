package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.domain.TenantRegistry;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.tenant.TenantLifecyclePolicy;
import com.phoenixtask.shared.tenant.TenantLifecycleStatus;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class TenantHealthService {

  private final TenantRegistryLookupService lookupService;
  private final TenantCredentialResolver credentialResolver;

  public TenantHealthService(
      TenantRegistryLookupService lookupService,
      TenantCredentialResolver credentialResolver
  ) {
    this.lookupService = lookupService;
    this.credentialResolver = credentialResolver;
  }

  public TenantHealthSnapshot healthFor(String code) {
    TenantRegistry tenant = lookupService.findByCode(code)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
    return evaluate(tenant);
  }

  public List<TenantHealthSnapshot> healthAll() {
    return lookupService.findAll().stream()
        .map(this::evaluate)
        .toList();
  }

  private TenantHealthSnapshot evaluate(TenantRegistry tenant) {
    TenantLifecycleStatus status = TenantLifecyclePolicy.parseOrThrow(tenant.getStatus());
    var credentials = credentialResolver.resolve(tenant);

    String dbConnectivity = "SKIPPED";
    String overallHealth = "UNAVAILABLE";
    String message = TenantLifecyclePolicy.messageFor(status);

    if (status == TenantLifecycleStatus.ACTIVE) {
      if (credentials.password() == null || credentials.password().isBlank()) {
        dbConnectivity = "UNKNOWN";
        overallHealth = "DEGRADED";
        message = "Tenant active but credentials are unavailable";
      } else if (checkDbConnectivity(tenant, credentials)) {
        dbConnectivity = "UP";
        overallHealth = "HEALTHY";
        message = "Tenant active and database reachable";
      } else {
        dbConnectivity = "DOWN";
        overallHealth = "DEGRADED";
        message = "Tenant active but database unreachable";
      }
    }

    return new TenantHealthSnapshot(
        tenant.getCode(),
        status.name(),
        credentials.mode(),
        credentials.source(),
        dbConnectivity,
        overallHealth,
        message
    );
  }

  private boolean checkDbConnectivity(
      TenantRegistry tenant,
      TenantCredentialResolver.ResolvedCredentials credentials
  ) {
    String url = String.format(
        "jdbc:postgresql://%s:%d/%s",
        tenant.getDbHost(),
        tenant.getDbPort(),
        tenant.getDbName()
    );
    DriverManager.setLoginTimeout(3);
    try (Connection connection = DriverManager.getConnection(
        url,
        credentials.username(),
        credentials.password()
    )) {
      return connection.isValid(2);
    } catch (SQLException ex) {
      return false;
    }
  }

  public record TenantHealthSnapshot(
      String tenantCode,
      String lifecycleStatus,
      String credentialMode,
      String credentialSource,
      String dbConnectivity,
      String overallHealth,
      String message
  ) {}
}
