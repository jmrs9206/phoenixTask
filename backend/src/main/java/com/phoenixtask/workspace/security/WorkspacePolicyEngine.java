package com.phoenixtask.workspace.security;

import com.phoenixtask.security.AuthPrincipal;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class WorkspacePolicyEngine {

  private final PermissionGrantProvider permissionGrantProvider;
  private final ContextRoleResolver contextRoleResolver;

  public WorkspacePolicyEngine(
      PermissionGrantProvider permissionGrantProvider,
      ContextRoleResolver contextRoleResolver
  ) {
    this.permissionGrantProvider = permissionGrantProvider;
    this.contextRoleResolver = contextRoleResolver;
  }

  public PolicyDecision check(AuthPrincipal principal, PermissionCode permission, PermissionContext context) {
    Optional<Long> roleId = contextRoleResolver.resolveRoleId(principal, context);
    if (roleId.isEmpty()) {
      return PolicyDecision.deny("role_not_found_for_context");
    }
    Set<String> permissionCodes = permissionGrantProvider.findPermissionCodesByRoleId(roleId.get());
    if (permissionCodes.contains(permission.code())) {
      return PolicyDecision.allow("permission_granted");
    }
    return PolicyDecision.deny("permission_missing");
  }

  public boolean isAllowed(AuthPrincipal principal, PermissionCode permission, PermissionContext context) {
    return check(principal, permission, context).allowed();
  }
}
