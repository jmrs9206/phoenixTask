package com.phoenixtask.security;

import java.util.Set;

public class PublicApiPrincipal {

  private final Long keyId;
  private final String tenantCode;
  private final String label;
  private final Set<String> scopes;

  public PublicApiPrincipal(Long keyId, String tenantCode, String label, Set<String> scopes) {
    this.keyId = keyId;
    this.tenantCode = tenantCode;
    this.label = label;
    this.scopes = scopes;
  }

  public Long getKeyId() {
    return keyId;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public String getLabel() {
    return label;
  }

  public Set<String> getScopes() {
    return scopes;
  }
}
