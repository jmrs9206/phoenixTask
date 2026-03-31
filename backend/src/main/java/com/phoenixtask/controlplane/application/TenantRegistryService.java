package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.domain.TenantRegistry;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryJpaRepository;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryMapper;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class TenantRegistryService {

  private final TenantRegistryJpaRepository repository;

  public TenantRegistryService(TenantRegistryJpaRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Optional<TenantRegistry> findByCode(String code) {
    return repository.findByCode(code).map(TenantRegistryMapper::toDomain);
  }

  @Transactional
  public TenantRegistry save(TenantRegistry tenant) {
    var entity = TenantRegistryMapper.toEntity(tenant);
    var saved = repository.save(entity);
    return TenantRegistryMapper.toDomain(saved);
  }
}
