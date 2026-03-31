package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.interfaces.PageRequest;
import com.phoenixtask.shared.interfaces.PageResponse;
import com.phoenixtask.workspace.application.dto.ProjectListQuery;
import com.phoenixtask.workspace.application.dto.ProjectResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceProjectRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceProjectService {

  private final WorkspaceProjectRepository repository;

  public WorkspaceProjectService(WorkspaceProjectRepository repository) {
    this.repository = repository;
  }

  public PageResponse<ProjectResponse> listProjects(PageRequest pageRequest, ProjectListQuery query) {
    long total = repository.countAll(query.status(), query.query());
    List<ProjectResponse> items = repository.findAllPaged(
            query.status(),
            query.query(),
            pageRequest.pageSize(),
            pageRequest.offset()
        ).stream()
        .map(row -> new ProjectResponse(
            row.id(),
            row.companyId(),
            row.projectKey(),
            row.name(),
            row.description(),
            row.status(),
            row.createdAt(),
            row.updatedAt()
        ))
        .collect(Collectors.toList());
    return PageResponse.of(items, pageRequest, total);
  }
}
