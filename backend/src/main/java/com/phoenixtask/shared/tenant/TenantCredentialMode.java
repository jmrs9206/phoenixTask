package com.phoenixtask.shared.tenant;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum TenantCredentialMode {
  SHARED,
  DEDICATED;

  public static Optional<TenantCredentialMode> from(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String normalized = raw.trim().toUpperCase(Locale.ROOT);
    for (TenantCredentialMode mode : values()) {
      if (mode.name().equals(normalized)) {
        return Optional.of(mode);
      }
    }
    return Optional.empty();
  }

  public static Set<String> allowedValues() {
    return Set.of(SHARED.name(), DEDICATED.name());
  }
}
