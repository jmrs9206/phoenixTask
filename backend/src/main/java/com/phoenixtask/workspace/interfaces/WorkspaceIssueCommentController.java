package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.WorkspaceIssueCommentService;
import com.phoenixtask.workspace.application.dto.IssueCommentCreateRequest;
import com.phoenixtask.workspace.application.dto.IssueCommentResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/issues/{issueId}/comments")
public class WorkspaceIssueCommentController {

  private final WorkspaceIssueCommentService service;

  public WorkspaceIssueCommentController(WorkspaceIssueCommentService service) {
    this.service = service;
  }

  @GetMapping
  public List<IssueCommentResponse> listComments(@PathVariable Long issueId) {
    return service.listComments(issueId);
  }

  @PostMapping
  public ResponseEntity<IssueCommentResponse> createComment(
      @AuthenticationPrincipal AuthPrincipal principal,
      @PathVariable Long issueId,
      @Valid @RequestBody IssueCommentCreateRequest request
  ) {
    IssueCommentResponse response = service.createComment(issueId, requireUserId(principal), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @AuthenticationPrincipal AuthPrincipal principal,
      @PathVariable Long issueId,
      @PathVariable Long commentId
  ) {
    Long userId = requireUserId(principal);
    service.deleteComment(issueId, commentId, userId);
    return ResponseEntity.noContent().build();
  }

  private Long requireUserId(AuthPrincipal principal) {
    if (principal == null || principal.getUserId() == null || principal.getUserId() <= 0) {
      throw new ValidationException("Authenticated user is required");
    }
    return principal.getUserId();
  }
}
