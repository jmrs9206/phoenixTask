package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.domain.TenantRegistry;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryJpaRepository;
import com.phoenixtask.controlplane.infrastructure.persistence.TenantRegistryMapper;
import com.phoenixtask.shared.tenant.TenantMetadata;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class TenantRegistryLookupService {

  private final ObjectProvider<TenantRegistryJpaRepository> repositoryProvider;

  public TenantRegistryLookupService(ObjectProvider<TenantRegistryJpaRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public Optional<TenantRegistry> findByCode(String code) {
    TenantRegistryJpaRepository repository = repositoryProvider.getIfAvailable();
    if (repository == null) {
      return Optional.empty();
    }
    return repository.findByCode(code).map(TenantRegistryMapper::toDomain);
  }

  public List<TenantRegistry> findAll() {
    TenantRegistryJpaRepository repository = repositoryProvider.getIfAvailable();
    if (repository == null) {
      return List.of();
    }
    return repository.findAll().stream()
        .map(TenantRegistryMapper::toDomain)
        .collect(Collectors.toList());
  }

  public Optional<TenantMetadata> findMetadataByCode(String code) {
    return findByCode(code).map(this::toMetadata);
  }

  private TenantMetadata toMetadata(TenantRegistry tenant) {
    return new TenantMetadata(
        tenant.getCode(),
        tenant.getDbHost(),
        tenant.getDbPort(),
        tenant.getDbName(),
        tenant.getDbSchema(),
        tenant.getDbUsername(),
        tenant.getStatus()
    );
  }
}
