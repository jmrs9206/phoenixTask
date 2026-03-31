package com.phoenixtask.workspace.application;

import com.phoenixtask.workspace.application.dto.UserResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceUserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceUserService {

  private final WorkspaceUserRepository repository;

  public WorkspaceUserService(WorkspaceUserRepository repository) {
    this.repository = repository;
  }

  public List<UserResponse> listUsers() {
    return repository.findAll().stream()
        .map(row -> new UserResponse(
            row.id(),
            row.companyId(),
            row.primaryRoleId(),
            row.firstName(),
            row.lastName(),
            row.email(),
            row.status(),
            row.createdAt(),
            row.updatedAt()
        ))
        .collect(Collectors.toList());
  }
}
