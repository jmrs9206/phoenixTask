package com.phoenixtask.workspace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMembershipRepository extends JpaRepository<TeamMembershipEntity, Long> {
}
