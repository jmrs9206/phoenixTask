package com.phoenixtask.publicapi.application;

import com.phoenixtask.publicapi.interfaces.dto.PublicApiIssueDetailResponse;
import com.phoenixtask.publicapi.interfaces.dto.PublicApiIssueResponse;
import com.phoenixtask.publicapi.interfaces.dto.PublicApiProjectDetailResponse;
import com.phoenixtask.publicapi.interfaces.dto.PublicApiProjectResponse;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.interfaces.PageRequest;
import com.phoenixtask.shared.interfaces.PageResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceProjectRepository;
import org.springframework.stereotype.Service;

@Service
public class PublicApiQueryService {

  private final WorkspaceProjectRepository projectRepository;
  private final WorkspaceIssueRepository issueRepository;

  public PublicApiQueryService(
      WorkspaceProjectRepository projectRepository,
      WorkspaceIssueRepository issueRepository
  ) {
    this.projectRepository = projectRepository;
    this.issueRepository = issueRepository;
  }

  public PageResponse<PublicApiProjectResponse> listProjects(
      PageRequest pageRequest,
      String status,
      String query
  ) {
    int limit = pageRequest.pageSize();
    int offset = pageRequest.offset();
    long total = projectRepository.countAll(status, query);
    var items = projectRepository.findAllPaged(status, query, limit, offset).stream()
        .map(row -> new PublicApiProjectResponse(
            row.id(),
            row.projectKey(),
            row.name(),
            row.status()
        ))
        .toList();
    return PageResponse.of(items, pageRequest, total);
  }

  public PublicApiProjectDetailResponse getProject(Long projectId) {
    WorkspaceProjectRepository.ProjectRow row = projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    return new PublicApiProjectDetailResponse(
        row.id(),
        row.projectKey(),
        row.name(),
        row.status(),
        row.description()
    );
  }

  public PageResponse<PublicApiIssueResponse> listIssues(
      PageRequest pageRequest,
      Long projectId,
      String status,
      String priority,
      String query
  ) {
    int limit = pageRequest.pageSize();
    int offset = pageRequest.offset();
    long total = issueRepository.countAll(projectId, status, priority, null, query);
    var items = issueRepository.findAllPaged(projectId, status, priority, null, query, limit, offset).stream()
        .map(row -> new PublicApiIssueResponse(
            row.id(),
            row.issueKey(),
            row.title(),
            row.projectKey(),
            row.status(),
            row.priority()
        ))
        .toList();
    return PageResponse.of(items, pageRequest, total);
  }

  public PublicApiIssueDetailResponse getIssue(Long issueId) {
    WorkspaceIssueRepository.IssueDetailRow row = issueRepository.findById(issueId)
        .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
    return new PublicApiIssueDetailResponse(
        row.id(),
        row.issueKey(),
        row.title(),
        row.projectKey(),
        row.status(),
        row.priority(),
        row.dueDate(),
        row.description()
    );
  }
}
