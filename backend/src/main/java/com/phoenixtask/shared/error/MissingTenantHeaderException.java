package com.phoenixtask.shared.error;

public class MissingTenantHeaderException extends RuntimeException {

  public MissingTenantHeaderException(String message) {
    super(message);
  }
}
