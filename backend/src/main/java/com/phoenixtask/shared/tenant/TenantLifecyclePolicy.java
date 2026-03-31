package com.phoenixtask.shared.tenant;

import com.phoenixtask.shared.error.ConflictException;
import com.phoenixtask.shared.error.TenantInactiveException;
import com.phoenixtask.shared.error.ValidationException;
import java.util.Locale;
import java.util.Set;

public final class TenantLifecyclePolicy {

  private TenantLifecyclePolicy() {}

  public static TenantLifecycleStatus requireActive(String rawStatus) {
    TenantLifecycleStatus status = parseOrThrow(rawStatus);
    if (status != TenantLifecycleStatus.ACTIVE) {
      throw new TenantInactiveException(messageFor(status));
    }
    return status;
  }

  public static TenantLifecycleStatus parseOrThrow(String rawStatus) {
    return TenantLifecycleStatus.from(rawStatus)
        .orElseThrow(() -> new ValidationException("Tenant status is invalid"));
  }

  public static String messageFor(TenantLifecycleStatus status) {
    String label = status.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    return "Tenant is " + label;
  }

  public static void requireTransition(
      TenantLifecycleStatus current,
      TenantLifecycleStatus target,
      Set<TenantLifecycleStatus> allowedFrom
  ) {
    if (!allowedFrom.contains(current)) {
      throw new ConflictException(
          "Tenant status must be " + allowedFrom + " to transition to " + target.name()
      );
    }
  }
}
