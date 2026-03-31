package com.phoenixtask.security;

import com.phoenixtask.controlplane.application.AccessLogService;
import com.phoenixtask.controlplane.infrastructure.persistence.AccessLogEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class AccessLogFilter extends OncePerRequestFilter {

  private final AccessLogService accessLogService;

  public AccessLogFilter(AccessLogService accessLogService) {
    this.accessLogService = accessLogService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) {
      return true;
    }
    return !(path.startsWith("/api/") || path.startsWith("/actuator/"));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    long start = System.currentTimeMillis();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long durationMs = System.currentTimeMillis() - start;
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String tenantCode = null;
      Long userId = null;
      String userEmail = null;
      if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
        tenantCode = principal.getTenantCode();
        userId = principal.getUserId();
        userEmail = principal.getEmail();
      } else if (authentication != null && authentication.getPrincipal() instanceof PublicApiPrincipal apiPrincipal) {
        tenantCode = apiPrincipal.getTenantCode();
      } else {
        String headerTenant = request.getHeader("X-Tenant-Code");
        if (headerTenant != null && !headerTenant.isBlank()) {
          tenantCode = headerTenant.trim();
        }
      }

      AccessLogEntity entity = new AccessLogEntity();
      Object requestIdAttr = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR);
      if (requestIdAttr != null) {
        entity.setRequestId(requestIdAttr.toString());
      }
      entity.setTenantCode(tenantCode);
      entity.setUserId(userId);
      entity.setUserEmail(userEmail);
      entity.setHttpMethod(request.getMethod());
      entity.setPath(request.getRequestURI());
      entity.setStatus(response.getStatus());
      entity.setIpAddress(resolveClientIp(request));
      entity.setUserAgent(request.getHeader("User-Agent"));
      entity.setDurationMs(durationMs);
      accessLogService.record(entity);
    }
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
