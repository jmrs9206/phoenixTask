package com.phoenixtask.controlplane.domain;

import java.time.LocalDateTime;

public class TenantRegistry {

  private Long id;
  private String code;
  private String name;
  private String dbHost;
  private Integer dbPort;
  private String dbName;
  private String dbSchema;
  private String dbUsername;
  private String credentialMode;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String status;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
