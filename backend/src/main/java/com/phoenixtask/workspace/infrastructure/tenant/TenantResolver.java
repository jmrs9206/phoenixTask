package com.phoenixtask.workspace.infrastructure.tenant;

import com.phoenixtask.controlplane.application.TenantRegistryLookupService;
import com.phoenixtask.shared.error.MissingTenantHeaderException;
import com.phoenixtask.shared.error.TenantNotFoundException;
import com.phoenixtask.shared.tenant.TenantMetadata;
import com.phoenixtask.shared.tenant.TenantLifecyclePolicy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class TenantResolver {

  private static final String TENANT_HEADER = "X-Tenant-Code";
  private final TenantRegistryLookupService lookupService;

  public TenantResolver(TenantRegistryLookupService lookupService) {
    this.lookupService = lookupService;
  }

  public TenantMetadata resolve(HttpServletRequest request) {
    if (TenantContext.get() != null) {
      return TenantContext.get();
    }

    String tenantCode = request.getHeader(TENANT_HEADER);
    if (tenantCode == null || tenantCode.isBlank()) {
      throw new MissingTenantHeaderException("X-Tenant-Code header is required");
    }

    TenantMetadata metadata = lookupService.findMetadataByCode(tenantCode)
        .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

    TenantLifecyclePolicy.requireActive(metadata.getStatus());

    TenantContext.set(metadata);
    return metadata;
  }
}
