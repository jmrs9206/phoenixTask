package com.phoenixtask.controlplane.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAuditEventJpaRepository extends JpaRepository<AuthAuditEventEntity, Long> {
}
