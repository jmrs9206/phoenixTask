package com.phoenixtask.controlplane.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRegistryJpaRepository extends JpaRepository<TenantRegistryEntity, Long> {
  Optional<TenantRegistryEntity> findByCode(String code);
}
