package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.IssueAttachmentResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueAttachmentRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueAttachmentRepository.IssueAttachmentRow;
import com.phoenixtask.workspace.infrastructure.storage.WorkspaceIssueAttachmentStorage;
import com.phoenixtask.workspace.infrastructure.storage.WorkspaceIssueAttachmentStorage.StoredAttachment;
import com.phoenixtask.workspace.infrastructure.tenant.TenantContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class WorkspaceIssueAttachmentService {

  private final WorkspaceIssueAttachmentRepository repository;
  private final WorkspaceIssueAttachmentStorage storage;
  private final WorkspaceIssueActivityService activityService;

  public WorkspaceIssueAttachmentService(
      WorkspaceIssueAttachmentRepository repository,
      WorkspaceIssueAttachmentStorage storage,
      WorkspaceIssueActivityService activityService
  ) {
    this.repository = repository;
    this.storage = storage;
    this.activityService = activityService;
  }

  public List<IssueAttachmentResponse> listAttachments(Long issueId) {
    return repository.findByIssue(issueId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public IssueAttachmentResponse uploadAttachment(Long issueId, Long uploaderUserId, MultipartFile file) {
    if (uploaderUserId == null || uploaderUserId <= 0) {
      throw new ValidationException("Authenticated user is required");
    }
    String tenantCode = TenantContext.getRequired().getTenantCode();
    StoredAttachment stored = storage.store(tenantCode, issueId, file);
    Long id = null;
    try {
      id = repository.insert(
          issueId,
          uploaderUserId,
          stored.originalFilename(),
          stored.storedFilename(),
          stored.contentType(),
          stored.sizeBytes()
      );
    } catch (RuntimeException ex) {
      storage.deleteIfExists(stored.path());
      throw ex;
    }

    IssueAttachmentRow row = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
    activityService.recordAttachmentUploaded(
        issueId,
        uploaderUserId,
        row.id(),
        row.originalFilename(),
        row.sizeBytes()
    );
    return toResponse(row);
  }

  public AttachmentFile loadAttachment(Long issueId, Long attachmentId) {
    IssueAttachmentRow row = repository.findById(attachmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
    if (!row.issueId().equals(issueId)) {
      throw new ResourceNotFoundException("Attachment not found");
    }
    String tenantCode = TenantContext.getRequired().getTenantCode();
    Path path = storage.resolvePath(tenantCode, issueId, row.storedFilename());
    if (!Files.exists(path)) {
      throw new ResourceNotFoundException("Attachment file not found");
    }
    return new AttachmentFile(row, path);
  }

  @Transactional
  public void deleteAttachment(Long issueId, Long attachmentId, Long actorUserId) {
    IssueAttachmentRow row = repository.findById(attachmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
    if (!row.issueId().equals(issueId)) {
      throw new ResourceNotFoundException("Attachment not found");
    }
    if (actorUserId == null || actorUserId <= 0) {
      throw new ValidationException("Authenticated user is required");
    }
    String tenantCode = TenantContext.getRequired().getTenantCode();
    Path path = storage.resolvePath(tenantCode, issueId, row.storedFilename());
    storage.deleteIfExists(path);
    repository.delete(attachmentId);
    activityService.recordAttachmentDeleted(
        issueId,
        actorUserId,
        row.id(),
        row.originalFilename(),
        row.sizeBytes()
    );
  }

  private IssueAttachmentResponse toResponse(IssueAttachmentRow row) {
    return new IssueAttachmentResponse(
        row.id(),
        row.issueId(),
        row.uploaderUserId(),
        row.uploaderName(),
        row.originalFilename(),
        row.mimeType(),
        row.sizeBytes(),
        row.createdAt()
    );
  }

  public record AttachmentFile(IssueAttachmentRow row, Path path) {}
}
