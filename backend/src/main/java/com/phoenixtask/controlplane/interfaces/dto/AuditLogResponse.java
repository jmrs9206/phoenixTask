package com.phoenixtask.controlplane.interfaces.dto;

import java.time.LocalDateTime;

public class AuditLogResponse {

  private final Long id;
  private final String tenantCode;
  private final String domain;
  private final String eventType;
  private final String actorType;
  private final Long actorId;
  private final String actorEmail;
  private final String resourceType;
  private final String resourceId;
  private final String outcome;
  private final String ipAddress;
  private final String userAgent;
  private final String requestId;
  private final String detail;
  private final LocalDateTime createdAt;

  public AuditLogResponse(
      Long id,
      String tenantCode,
      String domain,
      String eventType,
      String actorType,
      Long actorId,
      String actorEmail,
      String resourceType,
      String resourceId,
      String outcome,
      String ipAddress,
      String userAgent,
      String requestId,
      String detail,
      LocalDateTime createdAt
  ) {
    this.id = id;
    this.tenantCode = tenantCode;
    this.domain = domain;
    this.eventType = eventType;
    this.actorType = actorType;
    this.actorId = actorId;
    this.actorEmail = actorEmail;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.outcome = outcome;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.requestId = requestId;
    this.detail = detail;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public String getDomain() {
    return domain;
  }

  public String getEventType() {
    return eventType;
  }

  public String getActorType() {
    return actorType;
  }

  public Long getActorId() {
    return actorId;
  }

  public String getActorEmail() {
    return actorEmail;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getOutcome() {
    return outcome;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getDetail() {
    return detail;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
