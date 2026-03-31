package com.phoenixtask.controlplane.application.audit;

public record AuditEvent(
    String tenantCode,
    AuditDomain domain,
    String eventType,
    String actorType,
    Long actorId,
    String actorEmail,
    String resourceType,
    String resourceId,
    AuditOutcome outcome,
    String ipAddress,
    String userAgent,
    String requestId,
    String detail
) {}
