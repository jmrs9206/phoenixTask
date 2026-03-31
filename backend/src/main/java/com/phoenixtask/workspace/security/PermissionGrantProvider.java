package com.phoenixtask.workspace.security;

import java.util.Set;

public interface PermissionGrantProvider {
  Set<String> findPermissionCodesByRoleId(Long roleId);
}
