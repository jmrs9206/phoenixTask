package com.phoenixtask.shared.tenant;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum TenantLifecycleStatus {
  ACTIVE,
  SUSPENDED,
  FAILED,
  PENDING,
  DECOMMISSIONED;

  public static Optional<TenantLifecycleStatus> from(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String normalized = raw.trim().toUpperCase(Locale.ROOT);
    for (TenantLifecycleStatus status : values()) {
      if (status.name().equals(normalized)) {
        return Optional.of(status);
      }
    }
    return Optional.empty();
  }

  public static Set<String> allowedValues() {
    return Set.of(
        ACTIVE.name(),
        SUSPENDED.name(),
        FAILED.name(),
        PENDING.name(),
        DECOMMISSIONED.name()
    );
  }
}
