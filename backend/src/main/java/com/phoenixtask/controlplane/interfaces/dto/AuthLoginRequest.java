package com.phoenixtask.controlplane.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthLoginRequest {

  @NotBlank
  private String tenantCode;

  @NotBlank
  private String email;

  @NotBlank
  private String password;

  public String getTenantCode() {
    return tenantCode;
  }

  public void setTenantCode(String tenantCode) {
    this.tenantCode = tenantCode;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
