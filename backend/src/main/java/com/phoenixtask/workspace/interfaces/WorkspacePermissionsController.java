package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.workspace.application.WorkspacePermissionService;
import com.phoenixtask.workspace.application.dto.WorkspacePermissionsResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/permissions")
public class WorkspacePermissionsController {

  private final WorkspacePermissionService service;

  public WorkspacePermissionsController(WorkspacePermissionService service) {
    this.service = service;
  }

  @GetMapping("/me")
  public WorkspacePermissionsResponse getMyPermissions(
      @AuthenticationPrincipal AuthPrincipal principal
  ) {
    return service.getPermissions(principal);
  }
}
