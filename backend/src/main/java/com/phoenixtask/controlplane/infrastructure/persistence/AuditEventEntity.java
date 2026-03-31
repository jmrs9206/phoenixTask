package com.phoenixtask.controlplane.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_code", length = 64)
  private String tenantCode;

  @Column(name = "domain", nullable = false, length = 48)
  private String domain;

  @Column(name = "event_type", nullable = false, length = 64)
  private String eventType;

  @Column(name = "actor_type", length = 32)
  private String actorType;

  @Column(name = "actor_id")
  private Long actorId;

  @Column(name = "actor_email", length = 255)
  private String actorEmail;

  @Column(name = "resource_type", length = 64)
  private String resourceType;

  @Column(name = "resource_id", length = 128)
  private String resourceId;

  @Column(name = "outcome", length = 32)
  private String outcome;

  @Column(name = "ip_address", length = 64)
  private String ipAddress;

  @Column(name = "user_agent", length = 255)
  private String userAgent;

  @Column(name = "request_id", length = 64)
  private String requestId;

  @Column(name = "detail", length = 255)
  private String detail;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public void setTenantCode(String tenantCode) {
    this.tenantCode = tenantCode;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getActorType() {
    return actorType;
  }

  public void setActorType(String actorType) {
    this.actorType = actorType;
  }

  public Long getActorId() {
    return actorId;
  }

  public void setActorId(Long actorId) {
    this.actorId = actorId;
  }

  public String getActorEmail() {
    return actorEmail;
  }

  public void setActorEmail(String actorEmail) {
    this.actorEmail = actorEmail;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getOutcome() {
    return outcome;
  }

  public void setOutcome(String outcome) {
    this.outcome = outcome;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
