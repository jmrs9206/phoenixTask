package com.phoenixtask.controlplane.application;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.TenantNotFoundException;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.tenant.TenantMetadata;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspacePermissionRepository;
import com.phoenixtask.workspace.infrastructure.tenant.TenantContext;
import com.phoenixtask.workspace.security.PermissionCode;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ControlplanePublicApiAuthorizationService {

  private final TenantRegistryLookupService tenantRegistryLookupService;
  private final WorkspacePermissionRepository permissionRepository;

  public ControlplanePublicApiAuthorizationService(
      TenantRegistryLookupService tenantRegistryLookupService,
      WorkspacePermissionRepository permissionRepository
  ) {
    this.tenantRegistryLookupService = tenantRegistryLookupService;
    this.permissionRepository = permissionRepository;
  }

  public void requireManageKeys(AuthPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Missing Authorization header");
    }
    TenantMetadata metadata = tenantRegistryLookupService.findMetadataByCode(principal.getTenantCode())
        .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
    try {
      TenantContext.set(metadata);
      Set<String> codes = permissionRepository.findPermissionCodesByRoleId(principal.getPrimaryRoleId());
      if (!codes.contains(PermissionCode.PUBLIC_API_KEYS_MANAGE.code())) {
        throw new ForbiddenException("Missing permission: " + PermissionCode.PUBLIC_API_KEYS_MANAGE.code());
      }
    } finally {
      TenantContext.clear();
    }
  }
}
