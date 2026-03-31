package com.phoenixtask.shared.error;

public class TenantInactiveException extends RuntimeException {

  public TenantInactiveException(String message) {
    super(message);
  }
}
