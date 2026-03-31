package com.phoenixtask.controlplane.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicApiKeyJpaRepository extends JpaRepository<PublicApiKeyEntity, Long> {

  Optional<PublicApiKeyEntity> findByKeyHash(String keyHash);

  List<PublicApiKeyEntity> findByTenantCodeOrderByCreatedAtDesc(String tenantCode);
}
