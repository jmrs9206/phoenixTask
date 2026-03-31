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
public class ControlplaneLogAuthorizationService {

  private final TenantRegistryLookupService tenantRegistryLookupService;
  private final WorkspacePermissionRepository permissionRepository;

  public ControlplaneLogAuthorizationService(
      TenantRegistryLookupService tenantRegistryLookupService,
      WorkspacePermissionRepository permissionRepository
  ) {
    this.tenantRegistryLookupService = tenantRegistryLookupService;
    this.permissionRepository = permissionRepository;
  }

  public void requireAuditLogsView(AuthPrincipal principal) {
    requirePermission(principal, PermissionCode.AUDIT_LOGS_VIEW);
  }

  public void requireAccessLogsView(AuthPrincipal principal) {
    requirePermission(principal, PermissionCode.ACCESS_LOGS_VIEW);
  }

  private void requirePermission(AuthPrincipal principal, PermissionCode permission) {
    if (principal == null) {
      throw new UnauthorizedException("Missing Authorization header");
    }
    TenantMetadata metadata = tenantRegistryLookupService.findMetadataByCode(principal.getTenantCode())
        .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
    try {
      TenantContext.set(metadata);
      Set<String> codes = permissionRepository.findPermissionCodesByRoleId(principal.getPrimaryRoleId());
      if (!codes.contains(permission.code())) {
        throw new ForbiddenException("Missing permission: " + permission.code());
      }
    } finally {
      TenantContext.clear();
    }
  }
}
