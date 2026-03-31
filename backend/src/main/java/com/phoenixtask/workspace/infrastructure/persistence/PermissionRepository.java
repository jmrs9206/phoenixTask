package com.phoenixtask.workspace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
}
