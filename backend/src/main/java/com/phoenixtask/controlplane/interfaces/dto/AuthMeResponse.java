package com.phoenixtask.controlplane.interfaces.dto;

import java.time.LocalDateTime;

public class AuthMeResponse {

  private final String tenantCode;
  private final LocalDateTime expiresAt;
  private final AuthUserResponse user;

  public AuthMeResponse(
      String tenantCode,
      LocalDateTime expiresAt,
      AuthUserResponse user
  ) {
    this.tenantCode = tenantCode;
    this.expiresAt = expiresAt;
    this.user = user;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public AuthUserResponse getUser() {
    return user;
  }
}
