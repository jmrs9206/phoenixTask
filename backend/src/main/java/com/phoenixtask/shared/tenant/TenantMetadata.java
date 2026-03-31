package com.phoenixtask.shared.tenant;

public class TenantMetadata {

  private final String tenantCode;
  private final String dbHost;
  private final Integer dbPort;
  private final String dbName;
  private final String dbSchema;
  private final String dbUsername;
  private final String status;

  public TenantMetadata(
      String tenantCode,
      String dbHost,
      Integer dbPort,
      String dbName,
      String dbSchema,
      String dbUsername,
      String status
  ) {
    this.tenantCode = tenantCode;
    this.dbHost = dbHost;
    this.dbPort = dbPort;
    this.dbName = dbName;
    this.dbSchema = dbSchema;
    this.dbUsername = dbUsername;
    this.status = status;
  }

  public String getTenantCode() {
    return tenantCode;
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

  public String getStatus() {
    return status;
  }
}
