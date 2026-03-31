package com.phoenixtask.workspace.security;

import com.phoenixtask.security.AuthPrincipal;
import java.util.Optional;

public interface ContextRoleResolver {
  Optional<Long> resolveRoleId(AuthPrincipal principal, PermissionContext context);
}
