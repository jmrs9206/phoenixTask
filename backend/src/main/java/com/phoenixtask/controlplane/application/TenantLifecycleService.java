package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.domain.TenantRegistry;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryJpaRepository;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryMapper;
import com.phoenixtask.controlplane.application.audit.AuditDomain;
import com.phoenixtask.controlplane.application.audit.AuditLogService;
import com.phoenixtask.controlplane.application.audit.AuditOutcome;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.tenant.TenantLifecyclePolicy;
import com.phoenixtask.shared.tenant.TenantLifecycleStatus;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class TenantLifecycleService {

  private static final Logger log = LoggerFactory.getLogger(TenantLifecycleService.class);

  private final TenantRegistryJpaRepository repository;
  private final AuditLogService auditLogService;
  private final ObservabilityMetrics metrics;

  public TenantLifecycleService(
      TenantRegistryJpaRepository repository,
      AuditLogService auditLogService,
      ObservabilityMetrics metrics
  ) {
    this.repository = repository;
    this.auditLogService = auditLogService;
    this.metrics = metrics;
  }

  @Transactional
  public TenantRegistry suspendTenant(String code) {
    return changeStatus(code, TenantLifecycleStatus.SUSPENDED, Set.of(TenantLifecycleStatus.ACTIVE));
  }

  @Transactional
  public TenantRegistry reactivateTenant(String code) {
    return changeStatus(code, TenantLifecycleStatus.ACTIVE, Set.of(TenantLifecycleStatus.SUSPENDED));
  }

  private TenantRegistry changeStatus(
      String code,
      TenantLifecycleStatus target,
      Set<TenantLifecycleStatus> allowedFrom
  ) {
    TenantRegistryEntity entity = repository.findByCode(code)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
    TenantLifecycleStatus current = TenantLifecyclePolicy.parseOrThrow(entity.getStatus());
    TenantLifecyclePolicy.requireTransition(current, target, allowedFrom);
    if (current == target) {
      return TenantRegistryMapper.toDomain(entity);
    }
    entity.setStatus(target.name());
    TenantRegistryEntity saved = repository.save(entity);
    log.info("Tenant {} status changed from {} to {}", code, current.name(), target.name());
    auditLogService.recordSystemEvent(
        AuditDomain.TENANT,
        target == TenantLifecycleStatus.SUSPENDED ? "TENANT_SUSPENDED" : "TENANT_REACTIVATED",
        code,
        "TENANT",
        code,
        AuditOutcome.SUCCESS,
        null
    );
    if (target == TenantLifecycleStatus.SUSPENDED) {
      metrics.recordLifecycleEvent("suspended");
    } else if (target == TenantLifecycleStatus.ACTIVE) {
      metrics.recordLifecycleEvent("reactivated");
    }
    return TenantRegistryMapper.toDomain(saved);
  }
}
