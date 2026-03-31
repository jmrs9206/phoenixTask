package com.phoenixtask.workspace.infrastructure.tenant;

import com.phoenixtask.shared.tenant.TenantMetadata;

public final class TenantContext {

  private static final ThreadLocal<TenantMetadata> CURRENT = new ThreadLocal<>();

  private TenantContext() {}

  public static void set(TenantMetadata metadata) {
    CURRENT.set(metadata);
  }

  public static TenantMetadata get() {
    return CURRENT.get();
  }

  public static TenantMetadata getRequired() {
    TenantMetadata metadata = CURRENT.get();
    if (metadata == null) {
      throw new IllegalStateException("Tenant context not initialized");
    }
    return metadata;
  }

  public static void clear() {
    CURRENT.remove();
  }
}
