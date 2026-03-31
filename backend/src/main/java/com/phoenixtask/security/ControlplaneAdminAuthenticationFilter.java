package com.phoenixtask.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenixtask.controlplane.application.ControlplaneAdminKeyRecord;
import com.phoenixtask.controlplane.application.ControlplaneAdminKeyService;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.interfaces.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class ControlplaneAdminAuthenticationFilter extends OncePerRequestFilter {

  private static final String ADMIN_KEY_HEADER = "X-Controlplane-Admin-Key";

  private final ControlplaneAdminKeyService adminKeyService;
  private final ObjectMapper objectMapper;

  public ControlplaneAdminAuthenticationFilter(
      ControlplaneAdminKeyService adminKeyService,
      ObjectMapper objectMapper
  ) {
    this.adminKeyService = adminKeyService;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) {
      return true;
    }
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    if (!path.matches("^/api/controlplane/tenants/[^/]+/(suspend|reactivate)$")) {
      return true;
    }
    String header = request.getHeader(ADMIN_KEY_HEADER);
    return header == null || header.isBlank();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      String header = request.getHeader(ADMIN_KEY_HEADER);
      ControlplaneAdminKeyRecord record = adminKeyService.authenticate(header);
      ControlplaneAdminPrincipal principal = new ControlplaneAdminPrincipal(
          record.id(),
          record.label(),
          record.keyPrefix()
      );
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, null, List.of());
      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (UnauthorizedException ex) {
      writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private void writeError(
      HttpServletResponse response,
      HttpServletRequest request,
      int status,
      String message
  ) throws IOException {
    if (response.isCommitted()) {
      return;
    }
    ApiErrorResponse body = new ApiErrorResponse(
        OffsetDateTime.now().toString(),
        status,
        "UNAUTHORIZED",
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
