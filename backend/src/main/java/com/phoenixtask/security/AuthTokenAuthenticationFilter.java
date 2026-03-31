package com.phoenixtask.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenixtask.controlplane.application.AuthService;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.TenantInactiveException;
import com.phoenixtask.shared.error.TenantNotFoundException;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.shared.interfaces.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthTokenAuthenticationFilter extends OncePerRequestFilter {

  private final AuthService authService;
  private final ObjectMapper objectMapper;

  public AuthTokenAuthenticationFilter(
      AuthService authService,
      @Qualifier("objectMapper") ObjectMapper objectMapper
  ) {
    this.authService = authService;
    this.objectMapper = objectMapper;
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
    try {
      if (SecurityContextHolder.getContext().getAuthentication() != null
          && SecurityContextHolder.getContext().getAuthentication().getPrincipal()
          instanceof ControlplaneAdminPrincipal) {
        filterChain.doFilter(request, response);
        return;
      }
      String token = extractToken(request);
      if (token != null) {
        AuthPrincipal principal = shouldAllowInactiveTenant(request)
            ? authService.authenticateAllowInactive(token)
            : authService.authenticate(token);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
      filterChain.doFilter(request, response);
    } catch (UnauthorizedException ex) {
      writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
    } catch (ForbiddenException ex) {
      writeError(response, request, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", ex.getMessage());
    } catch (TenantNotFoundException ex) {
      writeError(response, request, HttpServletResponse.SC_NOT_FOUND, "TENANT_NOT_FOUND", ex.getMessage());
    } catch (TenantInactiveException ex) {
      writeError(response, request, HttpServletResponse.SC_CONFLICT, "TENANT_INACTIVE", ex.getMessage());
    } catch (ValidationException ex) {
      writeError(response, request, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private boolean shouldAllowInactiveTenant(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) {
      return false;
    }
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      return false;
    }
    return path.matches("^/api/controlplane/tenants/[^/]+/reactivate$");
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && !header.isBlank()) {
      if (!header.startsWith("Bearer ")) {
        throw new UnauthorizedException("Invalid Authorization header");
      }
      String token = header.substring("Bearer ".length()).trim();
      if (token.isEmpty()) {
        throw new UnauthorizedException("Invalid Authorization header");
      }
      return token;
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
