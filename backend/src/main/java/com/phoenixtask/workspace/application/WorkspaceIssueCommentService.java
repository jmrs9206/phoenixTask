package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.IssueCommentCreateRequest;
import com.phoenixtask.workspace.application.dto.IssueCommentResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueCommentRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueCommentRepository.IssueCommentRow;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceIssueCommentService {

  private final WorkspaceIssueCommentRepository repository;
  private final WorkspaceIssueActivityService activityService;

  public WorkspaceIssueCommentService(
      WorkspaceIssueCommentRepository repository,
      WorkspaceIssueActivityService activityService
  ) {
    this.repository = repository;
    this.activityService = activityService;
  }

  public List<IssueCommentResponse> listComments(Long issueId) {
    return repository.findByIssue(issueId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public IssueCommentResponse createComment(Long issueId, Long authorUserId, IssueCommentCreateRequest request) {
    if (authorUserId == null || authorUserId <= 0) {
      throw new ValidationException("Authenticated user is required");
    }
    String body = request.body() != null ? request.body().trim() : "";
    if (body.isBlank()) {
      throw new ValidationException("Comment body is required");
    }
    if (body.length() > 2000) {
      throw new ValidationException("Comment body exceeds 2000 characters");
    }
    Long id = repository.insert(issueId, authorUserId, body);
    IssueCommentRow row = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
    activityService.recordCommentCreated(issueId, authorUserId, row.id(), row.body());
    return toResponse(row);
  }

  @Transactional
  public void deleteComment(Long issueId, Long commentId, Long actorUserId) {
    IssueCommentRow row = repository.findById(commentId)
        .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
    if (!row.issueId().equals(issueId)) {
      throw new ResourceNotFoundException("Comment not found");
    }
    if (actorUserId == null || actorUserId <= 0) {
      throw new ValidationException("Authenticated user is required");
    }
    activityService.recordCommentDeleted(issueId, actorUserId, row.id(), row.body());
    repository.delete(commentId);
  }

  private IssueCommentResponse toResponse(IssueCommentRow row) {
    return new IssueCommentResponse(
        row.id(),
        row.issueId(),
        row.authorUserId(),
        row.authorName(),
        row.body(),
        row.createdAt()
    );
  }
}
