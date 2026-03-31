package com.phoenixtask.controlplane.interfaces;

import com.phoenixtask.controlplane.application.AccessLogQueryService;
import com.phoenixtask.controlplane.application.AuditLogQueryService;
import com.phoenixtask.controlplane.application.ControlplaneLogAuthorizationService;
import com.phoenixtask.controlplane.infrastructure.persistence.AccessLogEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.AuditEventEntity;
import com.phoenixtask.controlplane.interfaces.dto.AccessLogListResponse;
import com.phoenixtask.controlplane.interfaces.dto.AccessLogResponse;
import com.phoenixtask.controlplane.interfaces.dto.AuditLogListResponse;
import com.phoenixtask.controlplane.interfaces.dto.AuditLogResponse;
import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/controlplane")
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class LogController {

  private final AuditLogQueryService auditLogQueryService;
  private final AccessLogQueryService accessLogQueryService;
  private final ControlplaneLogAuthorizationService logAuthorizationService;

  public LogController(
      AuditLogQueryService auditLogQueryService,
      AccessLogQueryService accessLogQueryService,
      ControlplaneLogAuthorizationService logAuthorizationService
  ) {
    this.auditLogQueryService = auditLogQueryService;
    this.accessLogQueryService = accessLogQueryService;
    this.logAuthorizationService = logAuthorizationService;
  }

  @GetMapping("/audit-logs")
  public AuditLogListResponse listAuditLogs(
      @RequestParam(required = false) String tenantCode,
      @RequestParam(required = false) String domain,
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @RequestParam(required = false, defaultValue = "100") int limit
  ) {
    AuthPrincipal principal = requirePrincipal();
    logAuthorizationService.requireAuditLogsView(principal);
    List<AuditEventEntity> events = auditLogQueryService.query(
        tenantCode,
        domain,
        eventType,
        from,
        to,
        limit
    );
    List<AuditLogResponse> items = events.stream()
        .map(this::toAuditResponse)
        .collect(Collectors.toList());
    return new AuditLogListResponse(items);
  }

  @GetMapping("/access-logs")
  public AccessLogListResponse listAccessLogs(
      @RequestParam(required = false) String tenantCode,
      @RequestParam(required = false) String path,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @RequestParam(required = false, defaultValue = "100") int limit
  ) {
    AuthPrincipal principal = requirePrincipal();
    logAuthorizationService.requireAccessLogsView(principal);
    List<AccessLogEntity> events = accessLogQueryService.query(
        tenantCode,
        path,
        status,
        from,
        to,
        limit
    );
    List<AccessLogResponse> items = events.stream()
        .map(this::toAccessResponse)
        .collect(Collectors.toList());
    return new AccessLogListResponse(items);
  }

  private AuditLogResponse toAuditResponse(AuditEventEntity event) {
    return new AuditLogResponse(
        event.getId(),
        event.getTenantCode(),
        event.getDomain(),
        event.getEventType(),
        event.getActorType(),
        event.getActorId(),
        event.getActorEmail(),
        event.getResourceType(),
        event.getResourceId(),
        event.getOutcome(),
        event.getIpAddress(),
        event.getUserAgent(),
        event.getRequestId(),
        event.getDetail(),
        event.getCreatedAt()
    );
  }

  private AccessLogResponse toAccessResponse(AccessLogEntity event) {
    return new AccessLogResponse(
        event.getId(),
        event.getRequestId(),
        event.getTenantCode(),
        event.getUserId(),
        event.getUserEmail(),
        event.getHttpMethod(),
        event.getPath(),
        event.getStatus(),
        event.getIpAddress(),
        event.getUserAgent(),
        event.getDurationMs(),
        event.getCreatedAt()
    );
  }

  private AuthPrincipal requirePrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
      throw new UnauthorizedException("Missing Authorization header");
    }
    return principal;
  }
}
