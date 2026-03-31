package com.phoenixtask.controlplane.interfaces.dto;

import java.time.LocalDateTime;

public class AuthLoginResponse {

  private final String tokenType;
  private final String accessToken;
  private final LocalDateTime expiresAt;
  private final String tenantCode;
  private final AuthUserResponse user;

  public AuthLoginResponse(
      String tokenType,
      String accessToken,
      LocalDateTime expiresAt,
      String tenantCode,
      AuthUserResponse user
  ) {
    this.tokenType = tokenType;
    this.accessToken = accessToken;
    this.expiresAt = expiresAt;
    this.tenantCode = tenantCode;
    this.user = user;
  }

  public String getTokenType() {
    return tokenType;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public AuthUserResponse getUser() {
    return user;
  }
}
