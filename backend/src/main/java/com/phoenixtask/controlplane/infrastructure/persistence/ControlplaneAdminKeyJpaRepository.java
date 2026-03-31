package com.phoenixtask.controlplane.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlplaneAdminKeyJpaRepository
    extends JpaRepository<ControlplaneAdminKeyEntity, Long> {

  Optional<ControlplaneAdminKeyEntity> findByKeyHash(String keyHash);
}
