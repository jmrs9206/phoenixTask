package com.phoenixtask.publicapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenixtask.controlplane.application.TenantRegistryLookupService;
import com.phoenixtask.controlplane.application.audit.AuditDomain;
import com.phoenixtask.controlplane.application.audit.AuditEvent;
import com.phoenixtask.controlplane.application.audit.AuditLogService;
import com.phoenixtask.controlplane.application.audit.AuditOutcome;
import com.phoenixtask.publicapi.application.PublicApiKeyService;
import com.phoenixtask.publicapi.domain.PublicApiKeyRecord;
import com.phoenixtask.publicapi.domain.PublicApiScope;
import com.phoenixtask.security.PublicApiPrincipal;
import com.phoenixtask.security.RequestIdFilter;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.TenantInactiveException;
import com.phoenixtask.shared.error.TenantNotFoundException;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.shared.interfaces.ApiErrorResponse;
import com.phoenixtask.shared.tenant.TenantLifecyclePolicy;
import com.phoenixtask.shared.tenant.TenantMetadata;
import com.phoenixtask.workspace.infrastructure.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class PublicApiAuthenticationFilter extends OncePerRequestFilter {

  private static final String API_KEY_HEADER = "X-Phoenix-Api-Key";
  private static final String TENANT_HEADER = "X-Tenant-Code";

  private final PublicApiKeyService keyService;
  private final PublicApiScopeRegistry scopeRegistry;
  private final TenantRegistryLookupService tenantRegistryLookupService;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;

  public PublicApiAuthenticationFilter(
      PublicApiKeyService keyService,
      PublicApiScopeRegistry scopeRegistry,
      TenantRegistryLookupService tenantRegistryLookupService,
      AuditLogService auditLogService,
      ObjectMapper objectMapper
  ) {
    this.keyService = keyService;
    this.scopeRegistry = scopeRegistry;
    this.tenantRegistryLookupService = tenantRegistryLookupService;
    this.auditLogService = auditLogService;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) {
      return true;
    }
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    return !path.startsWith("/api/public/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    PublicApiKeyRecord keyRecord = null;
    PublicApiScope requiredScope = null;
    TenantMetadata previousTenant = TenantContext.get();
    try {
      String apiKey = extractKey(request);
      keyRecord = keyService.authenticate(apiKey);

      String headerTenant = request.getHeader(TENANT_HEADER);
      if (headerTenant != null && !headerTenant.isBlank()
          && !headerTenant.trim().equalsIgnoreCase(keyRecord.tenantCode())) {
        throw new ForbiddenException("Tenant mismatch for API key");
      }

      TenantMetadata metadata = tenantRegistryLookupService.findMetadataByCode(keyRecord.tenantCode())
          .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
      TenantLifecyclePolicy.requireActive(metadata.getStatus());

      TenantContext.set(metadata);

      PublicApiPrincipal principal = new PublicApiPrincipal(
          keyRecord.id(),
          keyRecord.tenantCode(),
          keyRecord.label(),
          keyRecord.scopes()
      );
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, null, List.of());
      SecurityContextHolder.getContext().setAuthentication(authentication);

      Optional<PublicApiScopeRule> rule = scopeRegistry.match(request.getMethod(), request.getRequestURI());
      if (rule.isEmpty()) {
        throw new ForbiddenException("No public API scope rule for this endpoint");
      }
      requiredScope = rule.get().scope();
      if (!keyRecord.scopes().contains(requiredScope.code())) {
        throw new ForbiddenException("Missing scope: " + requiredScope.code());
      }

      filterChain.doFilter(request, response);
      recordAudit(request, keyRecord, requiredScope, AuditOutcome.SUCCESS, null);
    } catch (UnauthorizedException ex) {
      recordAudit(request, keyRecord, requiredScope, AuditOutcome.FAILURE, ex.getMessage());
      writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
    } catch (ForbiddenException ex) {
      recordAudit(request, keyRecord, requiredScope, AuditOutcome.DENIED, ex.getMessage());
      writeError(response, request, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", ex.getMessage());
    } catch (TenantNotFoundException ex) {
      recordAudit(request, keyRecord, requiredScope, AuditOutcome.FAILURE, ex.getMessage());
      writeError(response, request, HttpServletResponse.SC_NOT_FOUND, "TENANT_NOT_FOUND", ex.getMessage());
    } catch (TenantInactiveException ex) {
      recordAudit(request, keyRecord, requiredScope, AuditOutcome.DENIED, ex.getMessage());
      writeError(response, request, HttpServletResponse.SC_CONFLICT, "TENANT_INACTIVE", ex.getMessage());
    } catch (ValidationException ex) {
      recordAudit(request, keyRecord, requiredScope, AuditOutcome.FAILURE, ex.getMessage());
      writeError(response, request, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    } finally {
      SecurityContextHolder.clearContext();
      if (previousTenant != null) {
        TenantContext.set(previousTenant);
      } else {
        TenantContext.clear();
      }
    }
  }

  private String extractKey(HttpServletRequest request) {
    String header = request.getHeader(API_KEY_HEADER);
    if (header == null || header.isBlank()) {
      throw new UnauthorizedException("Missing public API key");
    }
    return header.trim();
  }

  private void recordAudit(
      HttpServletRequest request,
      PublicApiKeyRecord keyRecord,
      PublicApiScope scope,
      AuditOutcome outcome,
      String detail
  ) {
    String requestId = Optional.ofNullable(request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR))
        .map(Object::toString)
        .orElse(null);
    AuditEvent event = new AuditEvent(
        keyRecord != null ? keyRecord.tenantCode() : null,
        AuditDomain.PUBLIC_API,
        "PUBLIC_API_REQUEST",
        "PUBLIC_API_KEY",
        keyRecord != null ? keyRecord.id() : null,
        null,
        "public_api",
        request.getMethod() + " " + request.getRequestURI(),
        outcome,
        request.getRemoteAddr(),
        request.getHeader("User-Agent"),
        requestId,
        buildDetail(scope, detail)
    );
    auditLogService.record(event);
  }

  private String buildDetail(PublicApiScope scope, String detail) {
    if (scope == null && (detail == null || detail.isBlank())) {
      return null;
    }
    String scopeDetail = scope != null ? "scope=" + scope.code() : null;
    if (detail == null || detail.isBlank()) {
      return scopeDetail;
    }
    return scopeDetail == null ? detail : scopeDetail + " | " + detail;
  }

  private void writeError(
      HttpServletResponse response,
      HttpServletRequest request,
      int status,
      String error,
      String message
  ) throws IOException {
    if (response.isCommitted()) {
      return;
    }
    ApiErrorResponse body = new ApiErrorResponse(
        OffsetDateTime.now().toString(),
        status,
        error,
        message,
        request.getRequestURI()
    );
    response.resetBuffer();
    response.setStatus(status);
    response.setContentType("application/json");
    objectMapper.writeValue(response.getOutputStream(), body);
    response.flushBuffer();
  }
}
