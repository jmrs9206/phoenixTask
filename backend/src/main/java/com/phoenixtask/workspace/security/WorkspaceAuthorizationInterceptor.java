package com.phoenixtask.workspace.security;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.controlplane.application.AuthService;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class WorkspaceAuthorizationInterceptor implements HandlerInterceptor {

  private final WorkspaceEndpointPermissionRegistry registry;
  private final WorkspacePermissionContextResolver contextResolver;
  private final WorkspacePolicyEngine policyEngine;
  private final AuthService authService;

  public WorkspaceAuthorizationInterceptor(
      WorkspaceEndpointPermissionRegistry registry,
      WorkspacePermissionContextResolver contextResolver,
      WorkspacePolicyEngine policyEngine,
      AuthService authService
  ) {
    this.registry = registry;
    this.contextResolver = contextResolver;
    this.policyEngine = policyEngine;
    this.authService = authService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    Optional<EndpointPermissionRule> rule = registry.match(request.getMethod(), request.getRequestURI());
    if (rule.isEmpty()) {
      if (handler instanceof org.springframework.web.method.HandlerMethod) {
        throw new ForbiddenException("Access denied");
      }
      return true;
    }
    AuthPrincipal principal = resolvePrincipal(request);
    ContextResolution resolution = contextResolver.resolve(rule.get(), principal, request);
    if (resolution.status() == ContextResolutionStatus.NOT_FOUND) {
      throw new ResourceNotFoundException("Resource not found");
    }
    PolicyDecision decision = policyEngine.check(principal, rule.get().permission(), resolution.context());
    if (!decision.allowed()) {
      throw new ForbiddenException("Access denied");
    }
    return true;
  }

  private AuthPrincipal resolvePrincipal(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    AuthPrincipal principal = null;
    if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal p) {
      principal = p;
    }
    if (principal == null) {
      String token = extractToken(request);
      if (token == null) {
        throw new UnauthorizedException("Missing Authorization header");
      }
      principal = authService.authenticate(token);
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(principal, null, List.of());
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }
    return principal;
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && !header.isBlank()) {
      if (!header.startsWith("Bearer ")) {
        throw new UnauthorizedException("Invalid Authorization header");
      }
      String token = header.substring("Bearer ".length()).trim();
      return token.isEmpty() ? null : token;
    }

    if (request.getCookies() == null) {
      return null;
    }
    for (var cookie : request.getCookies()) {
      if ("phoenixtask_auth".equals(cookie.getName())) {
        String value = cookie.getValue();
        if (value == null || value.isBlank()) {
          return null;
        }
        return value;
      }
    }
    return null;
  }
}
