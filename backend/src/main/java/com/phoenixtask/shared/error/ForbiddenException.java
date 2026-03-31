package com.phoenixtask.shared.error;

public class ForbiddenException extends RuntimeException {

  public ForbiddenException(String message) {
    super(message);
  }
}
