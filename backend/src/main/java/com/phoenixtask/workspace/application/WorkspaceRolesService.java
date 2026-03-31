package com.phoenixtask.workspace.application;

import com.phoenixtask.workspace.application.dto.RoleOptionResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceRoleRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceRolesService {

  private final WorkspaceRoleRepository repository;

  public WorkspaceRolesService(WorkspaceRoleRepository repository) {
    this.repository = repository;
  }

  public List<RoleOptionResponse> listAssignableRoles() {
    return repository.findAssignable().stream()
        .map(row -> new RoleOptionResponse(row.id(), row.code(), row.name()))
        .collect(Collectors.toList());
  }
}
