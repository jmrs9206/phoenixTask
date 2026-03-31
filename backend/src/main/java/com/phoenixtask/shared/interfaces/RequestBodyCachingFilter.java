package com.phoenixtask.shared.interfaces;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RequestBodyCachingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    if (shouldCache(request)) {
      byte[] cachedBody = request.getInputStream().readAllBytes();
      CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(request, cachedBody);
      filterChain.doFilter(wrapper, response);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean shouldCache(HttpServletRequest request) {
    String method = request.getMethod();
    if (!"POST".equalsIgnoreCase(method)
        && !"PUT".equalsIgnoreCase(method)
        && !"PATCH".equalsIgnoreCase(method)) {
      return false;
    }
    String contentType = request.getContentType();
    return contentType != null && contentType.contains("application/json");
  }
}
