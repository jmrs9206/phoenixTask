package com.phoenixtask.workspace.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_memberships")
public class TeamMembershipEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "team_id", nullable = false)
  private Long teamId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "role_id", nullable = false)
  private Long roleId;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
