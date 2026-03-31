package com.phoenixtask.controlplane.interfaces.dto;

import java.time.LocalDateTime;

public class AccessLogResponse {

  private final Long id;
  private final String requestId;
  private final String tenantCode;
  private final Long userId;
  private final String userEmail;
  private final String httpMethod;
  private final String path;
  private final Integer status;
  private final String ipAddress;
  private final String userAgent;
  private final Long durationMs;
  private final LocalDateTime createdAt;

  public AccessLogResponse(
      Long id,
      String requestId,
      String tenantCode,
      Long userId,
      String userEmail,
      String httpMethod,
      String path,
      Integer status,
      String ipAddress,
      String userAgent,
      Long durationMs,
      LocalDateTime createdAt
  ) {
    this.id = id;
    this.requestId = requestId;
    this.tenantCode = tenantCode;
    this.userId = userId;
    this.userEmail = userEmail;
    this.httpMethod = httpMethod;
    this.path = path;
    this.status = status;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.durationMs = durationMs;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public Long getUserId() {
    return userId;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getPath() {
    return path;
  }

  public Integer getStatus() {
    return status;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public Long getDurationMs() {
    return durationMs;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
