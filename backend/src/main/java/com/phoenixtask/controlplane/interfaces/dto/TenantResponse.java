package com.phoenixtask.controlplane.interfaces.dto;

import java.time.LocalDateTime;

public class TenantResponse {

  private final String code;
  private final String name;
  private final String dbHost;
  private final Integer dbPort;
  private final String dbName;
  private final String dbSchema;
  private final String dbUsername;
  private final String credentialMode;
  private final String status;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  public TenantResponse(
      String code,
      String name,
      String dbHost,
      Integer dbPort,
      String dbName,
      String dbSchema,
      String dbUsername,
      String credentialMode,
      String status,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {
    this.code = code;
    this.name = name;
    this.dbHost = dbHost;
    this.dbPort = dbPort;
    this.dbName = dbName;
    this.dbSchema = dbSchema;
    this.dbUsername = dbUsername;
    this.credentialMode = credentialMode;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getDbHost() {
    return dbHost;
  }

  public Integer getDbPort() {
    return dbPort;
  }

  public String getDbName() {
    return dbName;
  }

  public String getDbSchema() {
    return dbSchema;
  }

  public String getDbUsername() {
    return dbUsername;
  }

  public String getCredentialMode() {
    return credentialMode;
  }

  public String getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
