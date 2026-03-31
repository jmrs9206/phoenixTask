package com.phoenixtask.controlplane.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionEntity, Long> {

  Optional<AuthSessionEntity> findByTokenHash(String tokenHash);

  Optional<AuthSessionEntity> findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
      String tokenHash,
      LocalDateTime now
  );

  @Modifying
  @Transactional
  int deleteByExpiresAtBefore(LocalDateTime cutoff);
}
