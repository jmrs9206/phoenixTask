package com.phoenixtask.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "phoenixtask.cors")
public class CorsProperties {

  private List<String> allowedOrigins = List.of();
  private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
  private List<String> allowedHeaders = List.of(
      "Content-Type",
      "Authorization",
      "X-Tenant-Code",
      "X-User-Id"
  );
  private boolean allowCredentials = true;

  public List<String> getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(List<String> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  public List<String> getAllowedMethods() {
    return allowedMethods;
  }

  public void setAllowedMethods(List<String> allowedMethods) {
    this.allowedMethods = allowedMethods;
  }

  public List<String> getAllowedHeaders() {
    return allowedHeaders;
  }

  public void setAllowedHeaders(List<String> allowedHeaders) {
    this.allowedHeaders = allowedHeaders;
  }

  public boolean isAllowCredentials() {
    return allowCredentials;
  }

  public void setAllowCredentials(boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
  }
}
