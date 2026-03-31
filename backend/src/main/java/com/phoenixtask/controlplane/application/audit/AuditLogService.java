package com.phoenixtask.controlplane.application.audit;

import com.phoenixtask.controlplane.infrastructure.persistence.AuditEventEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.AuditEventJpaRepository;
import com.phoenixtask.security.AuthPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

  private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
  private static final String REQUEST_ID_ATTR = "requestId";

  private final AuditEventJpaRepository repository;

  public AuditLogService(AuditEventJpaRepository repository) {
    this.repository = repository;
  }

  public void record(AuditEvent event) {
    try {
      AuditEventEntity entity = new AuditEventEntity();
      entity.setTenantCode(event.tenantCode());
      entity.setDomain(event.domain().name());
      entity.setEventType(event.eventType());
      entity.setActorType(event.actorType());
      entity.setActorId(event.actorId());
      entity.setActorEmail(event.actorEmail());
      entity.setResourceType(event.resourceType());
      entity.setResourceId(event.resourceId());
      entity.setOutcome(event.outcome() != null ? event.outcome().name() : null);
      entity.setIpAddress(event.ipAddress());
      entity.setUserAgent(event.userAgent());
      entity.setRequestId(event.requestId());
      entity.setDetail(event.detail());
      repository.save(entity);
    } catch (Exception ex) {
      log.warn("Failed to persist audit event {}: {}", event.eventType(), ex.getMessage());
    }
  }

  public void recordFromRequest(
      AuditDomain domain,
      String eventType,
      String tenantCode,
      AuthPrincipal principal,
      String resourceType,
      String resourceId,
      AuditOutcome outcome,
      String detail,
      HttpServletRequest request
  ) {
    String requestId = Optional.ofNullable(request)
        .map(req -> req.getAttribute(REQUEST_ID_ATTR))
        .map(Object::toString)
        .orElse(null);
    String ip = request != null ? resolveClientIp(request) : null;
    String userAgent = request != null ? request.getHeader("User-Agent") : null;
    AuditEvent event = new AuditEvent(
        tenantCode,
        domain,
        eventType,
        principal != null ? "USER" : "SYSTEM",
        principal != null ? principal.getUserId() : null,
        principal != null ? principal.getEmail() : null,
        resourceType,
        resourceId,
        outcome,
        ip,
        userAgent,
        requestId,
        detail
    );
    record(event);
  }

  public void recordSystemEvent(
      AuditDomain domain,
      String eventType,
      String tenantCode,
      String resourceType,
      String resourceId,
      AuditOutcome outcome,
      String detail
  ) {
    AuditEvent event = new AuditEvent(
        tenantCode,
        domain,
        eventType,
        "SYSTEM",
        null,
        null,
        resourceType,
        resourceId,
        outcome,
        null,
        null,
        null,
        detail
    );
    record(event);
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      int commaIndex = forwarded.indexOf(',');
      return commaIndex > 0 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
    }
    return request.getRemoteAddr();
  }
}
