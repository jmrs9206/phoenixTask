package com.phoenixtask.shared.error;

public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}
