package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.WorkspaceIssueAttachmentService;
import com.phoenixtask.workspace.application.WorkspaceIssueAttachmentService.AttachmentFile;
import com.phoenixtask.workspace.application.dto.IssueAttachmentResponse;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/workspace/issues/{issueId}/attachments")
public class WorkspaceIssueAttachmentController {

  private final WorkspaceIssueAttachmentService service;

  public WorkspaceIssueAttachmentController(WorkspaceIssueAttachmentService service) {
    this.service = service;
  }

  @GetMapping
  public List<IssueAttachmentResponse> listAttachments(@PathVariable Long issueId) {
    return service.listAttachments(issueId);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<IssueAttachmentResponse> uploadAttachment(
      @AuthenticationPrincipal AuthPrincipal principal,
      @PathVariable Long issueId,
      @RequestPart("file") MultipartFile file
  ) {
    IssueAttachmentResponse response = service.uploadAttachment(issueId, requireUserId(principal), file);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{attachmentId}/download")
  public ResponseEntity<Resource> downloadAttachment(
      @PathVariable Long issueId,
      @PathVariable Long attachmentId
  ) {
    return buildFileResponse(service.loadAttachment(issueId, attachmentId), true);
  }

  @GetMapping("/{attachmentId}/preview")
  public ResponseEntity<Resource> previewAttachment(
      @PathVariable Long issueId,
      @PathVariable Long attachmentId
  ) {
    AttachmentFile attachment = service.loadAttachment(issueId, attachmentId);
    if (attachment.row().mimeType() == null || !attachment.row().mimeType().startsWith("image/")) {
      throw new ValidationException("Preview not available for this attachment");
    }
    return buildFileResponse(attachment, false);
  }

  @DeleteMapping("/{attachmentId}")
  public ResponseEntity<Void> deleteAttachment(
      @AuthenticationPrincipal AuthPrincipal principal,
      @PathVariable Long issueId,
      @PathVariable Long attachmentId
  ) {
    Long userId = requireUserId(principal);
    service.deleteAttachment(issueId, attachmentId, userId);
    return ResponseEntity.noContent().build();
  }

  private ResponseEntity<Resource> buildFileResponse(AttachmentFile attachment, boolean download) {
    Path path = attachment.path();
    Resource resource = new FileSystemResource(path.toFile());
    String filename = attachment.row().originalFilename();
    String disposition = download ? "attachment" : "inline";
    String contentType = attachment.row().mimeType() != null ? attachment.row().mimeType() : "application/octet-stream";
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + filename + "\"")
        .contentLength(attachment.row().sizeBytes())
        .body(resource);
  }

  private Long requireUserId(AuthPrincipal principal) {
    if (principal == null || principal.getUserId() == null || principal.getUserId() <= 0) {
      throw new ValidationException("Authenticated user is required");
    }
    return principal.getUserId();
  }
}
