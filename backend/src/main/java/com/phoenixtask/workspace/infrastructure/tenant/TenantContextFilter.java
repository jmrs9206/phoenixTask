package com.phoenixtask.workspace.infrastructure.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {

  private final TenantResolver tenantResolver;
  private final HandlerExceptionResolver exceptionResolver;

  public TenantContextFilter(
      TenantResolver tenantResolver,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
  ) {
    this.tenantResolver = tenantResolver;
    this.exceptionResolver = exceptionResolver;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    return path == null || !path.startsWith("/api/workspace/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      tenantResolver.resolve(request);
      filterChain.doFilter(request, response);
    } catch (RuntimeException ex) {
      exceptionResolver.resolveException(request, response, null, ex);
    } finally {
      TenantContext.clear();
    }
  }
}
