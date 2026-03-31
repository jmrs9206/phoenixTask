package com.phoenixtask.shared.error;

public class TenantNotFoundException extends RuntimeException {

  public TenantNotFoundException(String message) {
    super(message);
  }
}
