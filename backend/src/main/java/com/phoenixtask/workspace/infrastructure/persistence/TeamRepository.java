package com.phoenixtask.workspace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
}
