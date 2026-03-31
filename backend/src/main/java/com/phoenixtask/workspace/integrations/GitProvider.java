package com.phoenixtask.workspace.integrations;

import java.util.Arrays;
import java.util.Optional;

public enum GitProvider {
  GITHUB,
  GITLAB;

  public static Optional<GitProvider> from(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    return Arrays.stream(values())
        .filter(provider -> provider.name().equalsIgnoreCase(value))
        .findFirst();
  }
}
