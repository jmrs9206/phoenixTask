package com.phoenixtask.workspace.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.IssueActivityResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueActivityRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueActivityRepository.IssueActivityRow;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceIssueActivityService {

  private final WorkspaceIssueActivityRepository repository;
  private final ObjectMapper objectMapper;

  public WorkspaceIssueActivityService(
      WorkspaceIssueActivityRepository repository,
      ObjectMapper objectMapper
  ) {
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  public List<IssueActivityResponse> listActivity(Long issueId) {
    return repository.findByIssue(issueId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public void recordIssueCreated(Long issueId, Long actorUserId, String issueKey, String title) {
    recordEvent(issueId, actorUserId, IssueActivityType.ISSUE_CREATED, Map.of(
        "issueKey", issueKey,
        "title", title
    ));
  }

  public void recordCommentCreated(Long issueId, Long actorUserId, Long commentId, String body) {
    recordEvent(issueId, actorUserId, IssueActivityType.COMMENT_CREATED, Map.of(
        "commentId", commentId,
        "preview", preview(body)
    ));
  }

  public void recordCommentDeleted(Long issueId, Long actorUserId, Long commentId, String body) {
    recordEvent(issueId, actorUserId, IssueActivityType.COMMENT_DELETED, Map.of(
        "commentId", commentId,
        "preview", preview(body)
    ));
  }

  public void recordAttachmentUploaded(Long issueId, Long actorUserId, Long attachmentId, String filename, long sizeBytes) {
    recordEvent(issueId, actorUserId, IssueActivityType.ATTACHMENT_UPLOADED, Map.of(
        "attachmentId", attachmentId,
        "filename", filename,
        "sizeBytes", sizeBytes
    ));
  }

  public void recordAttachmentDeleted(Long issueId, Long actorUserId, Long attachmentId, String filename, long sizeBytes) {
    recordEvent(issueId, actorUserId, IssueActivityType.ATTACHMENT_DELETED, Map.of(
        "attachmentId", attachmentId,
        "filename", filename,
        "sizeBytes", sizeBytes
    ));
  }

  private void recordEvent(Long issueId, Long actorUserId, IssueActivityType type, Map<String, Object> metadata) {
    if (issueId == null || issueId <= 0) {
      throw new ValidationException("Issue id is required");
    }
    if (actorUserId == null || actorUserId <= 0) {
      throw new ValidationException("Actor is required");
    }
    String json = "{}";
    try {
      json = objectMapper.writeValueAsString(metadata == null ? Collections.emptyMap() : metadata);
    } catch (Exception ex) {
      throw new ValidationException("Unable to serialize activity metadata");
    }
    repository.insert(issueId, actorUserId, type.name(), json);
  }

  private IssueActivityResponse toResponse(IssueActivityRow row) {
    Map<String, Object> metadata = Collections.emptyMap();
    if (row.metadataJson() != null && !row.metadataJson().isBlank()) {
      try {
        metadata = objectMapper.readValue(row.metadataJson(), new TypeReference<>() {});
      } catch (Exception ex) {
        metadata = Collections.emptyMap();
      }
    }
    return new IssueActivityResponse(
        row.id(),
        row.issueId(),
        row.actorUserId(),
        row.actorName(),
        row.eventType(),
        metadata,
        row.createdAt()
    );
  }

  private String preview(String body) {
    if (body == null) {
      return "";
    }
    String trimmed = body.trim();
    if (trimmed.length() <= 120) {
      return trimmed;
    }
    return trimmed.substring(0, 120) + "…";
  }
}
