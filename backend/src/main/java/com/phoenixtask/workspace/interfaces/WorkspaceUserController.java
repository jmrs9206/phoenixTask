package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceUserService;
import com.phoenixtask.workspace.application.WorkspaceRolesService;
import com.phoenixtask.workspace.application.dto.RoleOptionResponse;
import com.phoenixtask.workspace.application.dto.UserResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/users")
public class WorkspaceUserController {

  private final WorkspaceUserService service;
  private final WorkspaceRolesService rolesService;

  public WorkspaceUserController(
      WorkspaceUserService service,
      WorkspaceRolesService rolesService
  ) {
    this.service = service;
    this.rolesService = rolesService;
  }

  @GetMapping
  public List<UserResponse> listUsers() {
    return service.listUsers();
  }

  @GetMapping("/roles")
  public List<RoleOptionResponse> listAssignableRoles() {
    return rolesService.listAssignableRoles();
  }
}
