package com.phoenixtask.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenixtask.shared.interfaces.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitingFilter extends OncePerRequestFilter {

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
  private static final List<RateLimitPolicy> POLICIES = List.of(
      new RateLimitPolicy("auth_login", "/api/auth/login", 5, 60, RateLimitKeyStrategy.IP),
      new RateLimitPolicy("auth_logout", "/api/auth/logout", 30, 60, RateLimitKeyStrategy.USER_TENANT),
      new RateLimitPolicy("auth_me", "/api/auth/me", 120, 60, RateLimitKeyStrategy.USER_TENANT),
      new RateLimitPolicy("workspace", "/api/workspace/**", 120, 60, RateLimitKeyStrategy.USER_TENANT),
      new RateLimitPolicy("controlplane", "/api/controlplane/**", 60, 60, RateLimitKeyStrategy.IP)
  );

  private final RateLimiterService rateLimiterService;
  private final ObjectMapper objectMapper;
  private final com.phoenixtask.controlplane.application.ObservabilityMetrics metrics;

  public RateLimitingFilter(
      RateLimiterService rateLimiterService,
      ObjectMapper objectMapper,
      com.phoenixtask.controlplane.application.ObservabilityMetrics metrics
  ) {
    this.rateLimiterService = rateLimiterService;
    this.objectMapper = objectMapper;
    this.metrics = metrics;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) {
      return true;
    }
    if (!path.startsWith("/api/")) {
      return true;
    }
    return path.equals("/api/system/status") || path.equals("/api/integrations/status");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String path = request.getRequestURI();
    RateLimitPolicy policy = matchPolicy(path);
    if (policy == null) {
      filterChain.doFilter(request, response);
      return;
    }

    String key = buildKey(policy, request);
    RateLimitDecision decision = rateLimiterService.evaluate(
        policy.name() + ":" + key,
        policy.capacity(),
        policy.windowSeconds()
    );

    response.setHeader("X-RateLimit-Limit", String.valueOf(policy.capacity()));
    response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
    response.setHeader("X-RateLimit-Reset", String.valueOf(decision.resetEpochSeconds()));

    if (!decision.allowed()) {
      metrics.recordRateLimitHit(policy.name());
      int status = 429;
      response.setStatus(status);
      response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      ApiErrorResponse body = new ApiErrorResponse(
          OffsetDateTime.now().toString(),
          status,
          "RATE_LIMITED",
          "Too many requests",
          request.getRequestURI()
      );
      objectMapper.writeValue(response.getOutputStream(), body);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private RateLimitPolicy matchPolicy(String path) {
    for (RateLimitPolicy policy : POLICIES) {
      if (PATH_MATCHER.match(policy.pathPattern(), path)) {
        return policy;
      }
    }
    return null;
  }

  private String buildKey(RateLimitPolicy policy, HttpServletRequest request) {
    return switch (policy.keyStrategy()) {
      case IP -> resolveClientIp(request);
      case TENANT -> resolveTenantKey(request).orElse(resolveClientIp(request));
      case USER -> resolveUserKey(request).orElse(resolveClientIp(request));
      case USER_TENANT -> resolveUserTenantKey(request).orElse(resolveClientIp(request));
    };
  }

  private Optional<String> resolveUserKey(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
      return Optional.of(principal.getUserId().toString());
    }
    return Optional.empty();
  }

  private Optional<String> resolveTenantKey(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
      return Optional.of(principal.getTenantCode());
    }
    String header = request.getHeader("X-Tenant-Code");
    if (header != null && !header.isBlank()) {
      return Optional.of(header.trim());
    }
    return Optional.empty();
  }

  private Optional<String> resolveUserTenantKey(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
      return Optional.of(principal.getTenantCode() + ":" + principal.getUserId());
    }
    return Optional.empty();
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
