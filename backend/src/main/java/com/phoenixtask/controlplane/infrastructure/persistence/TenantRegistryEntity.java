package com.phoenixtask.controlplane.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_registry")
public class TenantRegistryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String code;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(name = "db_host", nullable = false, length = 255)
  private String dbHost;

  @Column(name = "db_port", nullable = false)
  private Integer dbPort;

  @Column(name = "db_name", nullable = false, length = 128)
  private String dbName;

  @Column(name = "db_schema", length = 128)
  private String dbSchema;

  @Column(name = "db_username", nullable = false, length = 128)
  private String dbUsername;

  @Column(name = "credential_mode", nullable = false, length = 16)
  private String credentialMode;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false, length = 32)
  private String status;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDbHost() {
    return dbHost;
  }

  public void setDbHost(String dbHost) {
    this.dbHost = dbHost;
  }

  public Integer getDbPort() {
    return dbPort;
  }

  public void setDbPort(Integer dbPort) {
    this.dbPort = dbPort;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getDbSchema() {
    return dbSchema;
  }

  public void setDbSchema(String dbSchema) {
    this.dbSchema = dbSchema;
  }

  public String getDbUsername() {
    return dbUsername;
  }

  public void setDbUsername(String dbUsername) {
    this.dbUsername = dbUsername;
  }

  public String getCredentialMode() {
    return credentialMode;
  }

  public void setCredentialMode(String credentialMode) {
    this.credentialMode = credentialMode;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
