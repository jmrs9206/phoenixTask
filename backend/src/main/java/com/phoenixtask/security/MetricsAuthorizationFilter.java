package com.phoenixtask.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenixtask.controlplane.application.ControlplaneMetricsAuthorizationService;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.interfaces.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class MetricsAuthorizationFilter extends OncePerRequestFilter {

  private final ControlplaneMetricsAuthorizationService metricsAuthorizationService;
  private final ObjectMapper objectMapper;

  public MetricsAuthorizationFilter(
      ControlplaneMetricsAuthorizationService metricsAuthorizationService,
      ObjectMapper objectMapper
  ) {
    this.metricsAuthorizationService = metricsAuthorizationService;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) {
      return true;
    }
    return !(path.equals("/actuator/metrics") || path.startsWith("/actuator/metrics/"));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      AuthPrincipal principal = authentication != null && authentication.getPrincipal() instanceof AuthPrincipal p
          ? p
          : null;
      metricsAuthorizationService.requireMetricsView(principal);
      filterChain.doFilter(request, response);
    } catch (UnauthorizedException ex) {
      writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
    } catch (ForbiddenException ex) {
      writeError(response, request, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", ex.getMessage());
    }
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
