package com.phoenixtask.controlplane.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "controlplane_admin_keys")
public class ControlplaneAdminKeyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "label", nullable = false)
  private String label;

  @Column(name = "key_prefix", nullable = false)
  private String keyPrefix;

  @Column(name = "key_hash", nullable = false)
  private String keyHash;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "revoked_at")
  private OffsetDateTime revokedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getKeyPrefix() {
    return keyPrefix;
  }

  public void setKeyPrefix(String keyPrefix) {
    this.keyPrefix = keyPrefix;
  }

  public String getKeyHash() {
    return keyHash;
  }

  public void setKeyHash(String keyHash) {
    this.keyHash = keyHash;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(OffsetDateTime revokedAt) {
    this.revokedAt = revokedAt;
  }
}
