package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ConflictException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.IssueCreateRequest;
import com.phoenixtask.workspace.application.dto.IssueCreateResponse;
import com.phoenixtask.workspace.application.dto.IssueDetailResponse;
import com.phoenixtask.workspace.application.dto.IssueListItemResponse;
import com.phoenixtask.workspace.application.dto.IssueListQuery;
import com.phoenixtask.workspace.application.dto.IssueSummaryResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceProjectRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceUserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.phoenixtask.shared.interfaces.PageRequest;
import com.phoenixtask.shared.interfaces.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceIssueService {

  private static final Set<String> STATUSES = Set.of("OPEN", "IN_PROGRESS", "BLOCKED", "DONE");
  private static final Set<String> PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

  private final WorkspaceIssueRepository issueRepository;
  private final WorkspaceProjectRepository projectRepository;
  private final WorkspaceUserRepository userRepository;
  private final WorkspaceIssueActivityService activityService;

  public WorkspaceIssueService(
      WorkspaceIssueRepository issueRepository,
      WorkspaceProjectRepository projectRepository,
      WorkspaceUserRepository userRepository,
      WorkspaceIssueActivityService activityService
  ) {
    this.issueRepository = issueRepository;
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.activityService = activityService;
  }

  public PageResponse<IssueListItemResponse> listIssuesForUser(
      Long userId,
      IssueListQuery query,
      PageRequest pageRequest
  ) {
    if (userId == null || userId <= 0) {
      throw new ValidationException("Authenticated user is required");
    }
    long total = issueRepository.countAllForUser(
        userId,
        query.projectId(),
        query.status(),
        query.priority(),
        query.assigneeId(),
        query.query()
    );
    List<IssueListItemResponse> items = issueRepository.findAllForUserPaged(
            userId,
            query.projectId(),
            query.status(),
            query.priority(),
            query.assigneeId(),
            query.query(),
            pageRequest.pageSize(),
            pageRequest.offset()
        ).stream()
        .map(row -> new IssueListItemResponse(
            row.id(),
            row.issueKey(),
            row.title(),
            row.projectKey(),
            row.status(),
            row.priority(),
            row.assigneeName(),
            row.createdAt()
        ))
        .collect(Collectors.toList());
    return PageResponse.of(items, pageRequest, total);
  }

  public List<IssueSummaryResponse> listIssuesByProject(Long projectId) {
    if (!projectRepository.existsById(projectId)) {
      throw new ResourceNotFoundException("Project not found");
    }
    return issueRepository.findByProject(projectId).stream()
        .map(row -> new IssueSummaryResponse(
            row.id(),
            row.issueKey(),
            row.title(),
            row.status(),
            row.priority(),
            row.assigneeName()
        ))
        .collect(Collectors.toList());
  }

  public IssueDetailResponse getIssue(Long issueId) {
    WorkspaceIssueRepository.IssueDetailRow row = issueRepository.findById(issueId)
        .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
    return new IssueDetailResponse(
        row.id(),
        row.issueKey(),
        row.title(),
        row.description(),
        row.projectKey(),
        row.status(),
        row.priority(),
        row.reporterName(),
        row.assigneeName(),
        row.dueDate(),
        row.createdAt()
    );
  }

  @Transactional
  public IssueCreateResponse createIssue(IssueCreateRequest request) {
    validateStatus(request.status());
    validatePriority(request.priority());
    WorkspaceProjectRepository.ProjectKeyRow project = projectRepository
        .findProjectKey(request.projectId())
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

    if (!userRepository.existsById(request.reporterUserId())) {
      throw new ResourceNotFoundException("Reporter not found");
    }
    Long reporterCompanyId = issueRepository.findCompanyIdForUser(request.reporterUserId());
    Long companyId = projectRepository.getCompanyId();
    if (companyId == null) {
      throw new ResourceNotFoundException("Company not found");
    }
    if (reporterCompanyId == null || !companyId.equals(reporterCompanyId)) {
      throw new ValidationException("Reporter must belong to company");
    }
    if (request.assigneeUserId() != null) {
      if (!userRepository.existsById(request.assigneeUserId())) {
        throw new ResourceNotFoundException("Assignee not found");
      }
      Long assigneeCompanyId = issueRepository.findCompanyIdForUser(request.assigneeUserId());
      if (assigneeCompanyId == null || !companyId.equals(assigneeCompanyId)) {
        throw new ValidationException("Assignee must belong to company");
      }
      if (!issueRepository.isUserInProject(request.projectId(), request.assigneeUserId())) {
        throw new ValidationException("Assignee must belong to project");
      }
    }

    WorkspaceIssueRepository.IssueKeyRow keyRow = issueRepository.incrementIssueCounter(request.projectId());
    if (keyRow == null || keyRow.counter() == null) {
      throw new ResourceNotFoundException("Project not found");
    }
    String issueKey = project.projectKey() + "-" + keyRow.counter();

    Long id = issueRepository.insertIssue(
        request.projectId(),
        issueKey,
        request.title().trim(),
        normalizeDescription(request.description()),
        request.status(),
        request.priority(),
        request.reporterUserId(),
        request.assigneeUserId()
    );

    if (id == null) {
      throw new ConflictException("Issue key conflict");
    }

    activityService.recordIssueCreated(id, request.reporterUserId(), issueKey, request.title().trim());
    return new IssueCreateResponse(id, issueKey);
  }

  private void validateStatus(String status) {
    if (!STATUSES.contains(status)) {
      throw new ValidationException("Invalid issue status");
    }
  }

  private void validatePriority(String priority) {
    if (!PRIORITIES.contains(priority)) {
      throw new ValidationException("Invalid issue priority");
    }
  }

  private String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return description.trim();
  }
}
