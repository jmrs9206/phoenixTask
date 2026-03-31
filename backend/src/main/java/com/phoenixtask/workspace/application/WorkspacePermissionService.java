package com.phoenixtask.workspace.application;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.WorkspacePermissionsResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspacePermissionRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class WorkspacePermissionService {

  private final WorkspacePermissionRepository permissionRepository;

  public WorkspacePermissionService(WorkspacePermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  public WorkspacePermissionsResponse getPermissions(AuthPrincipal principal) {
    if (principal == null || principal.getPrimaryRoleId() == null) {
      throw new ValidationException("Authenticated role is required");
    }
    Set<String> codes = permissionRepository.findPermissionCodesByRoleId(principal.getPrimaryRoleId());
    List<String> sorted = codes.stream().sorted(Comparator.naturalOrder()).toList();
    return new WorkspacePermissionsResponse(principal.getPrimaryRoleId(), sorted);
  }
}
