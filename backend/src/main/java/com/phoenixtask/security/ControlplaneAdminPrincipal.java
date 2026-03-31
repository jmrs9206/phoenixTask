package com.phoenixtask.security;

public class ControlplaneAdminPrincipal {

  private final Long keyId;
  private final String label;
  private final String keyPrefix;

  public ControlplaneAdminPrincipal(Long keyId, String label, String keyPrefix) {
    this.keyId = keyId;
    this.label = label;
    this.keyPrefix = keyPrefix;
  }

  public Long getKeyId() {
    return keyId;
  }

  public String getLabel() {
    return label;
  }

  public String getKeyPrefix() {
    return keyPrefix;
  }
}
