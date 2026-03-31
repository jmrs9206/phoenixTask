package com.phoenixtask.controlplane.interfaces.dto;

public class TenantHealthResponse {

  private final String tenantCode;
  private final String lifecycleStatus;
  private final String credentialMode;
  private final String credentialSource;
  private final String dbConnectivity;
  private final String overallHealth;
  private final String message;

  public TenantHealthResponse(
      String tenantCode,
      String lifecycleStatus,
      String credentialMode,
      String credentialSource,
      String dbConnectivity,
      String overallHealth,
      String message
  ) {
    this.tenantCode = tenantCode;
    this.lifecycleStatus = lifecycleStatus;
    this.credentialMode = credentialMode;
    this.credentialSource = credentialSource;
    this.dbConnectivity = dbConnectivity;
    this.overallHealth = overallHealth;
    this.message = message;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public String getLifecycleStatus() {
    return lifecycleStatus;
  }

  public String getCredentialMode() {
    return credentialMode;
  }

  public String getCredentialSource() {
    return credentialSource;
  }

  public String getDbConnectivity() {
    return dbConnectivity;
  }

  public String getOverallHealth() {
    return overallHealth;
  }

  public String getMessage() {
    return message;
  }
}
