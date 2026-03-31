package com.phoenixtask.controlplane.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TenantCreateRequest {

  @NotBlank
  private String code;

  @NotBlank
  private String name;

  @NotBlank
  private String dbHost;

  @NotNull
  @Positive
  private Integer dbPort;

  @NotBlank
  private String dbName;

  private String dbSchema;

  @NotBlank
  private String dbUsername;

  private String status;

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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
