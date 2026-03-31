package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.WorkspaceMessageService;
import com.phoenixtask.workspace.application.dto.DirectThreadRequest;
import com.phoenixtask.workspace.application.dto.DirectThreadResponse;
import com.phoenixtask.workspace.application.dto.MessageCreateRequest;
import com.phoenixtask.workspace.application.dto.MessageItemResponse;
import com.phoenixtask.workspace.application.dto.MessageThreadDirectResponse;
import com.phoenixtask.workspace.application.dto.MessageThreadProjectResponse;
import com.phoenixtask.workspace.application.dto.MessageThreadTeamResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.phoenixtask.security.AuthPrincipal;

@RestController
@RequestMapping("/api/workspace/messages")
public class WorkspaceMessagesController {

  private final WorkspaceMessageService service;

  public WorkspaceMessagesController(WorkspaceMessageService service) {
    this.service = service;
  }

  @GetMapping("/teams")
  public List<MessageThreadTeamResponse> listTeamThreads(
      @AuthenticationPrincipal AuthPrincipal principal
  ) {
    return service.listTeamThreads(requireUserId(principal));
  }

  @GetMapping("/projects")
  public List<MessageThreadProjectResponse> listProjectThreads(
      @AuthenticationPrincipal AuthPrincipal principal
  ) {
    return service.listProjectThreads(requireUserId(principal));
  }

  @GetMapping("/direct")
  public List<MessageThreadDirectResponse> listDirectThreads(
      @AuthenticationPrincipal AuthPrincipal principal
  ) {
    return service.listDirectThreads(requireUserId(principal));
  }

  @GetMapping("/threads/{threadId}/messages")
  public List<MessageItemResponse> listMessages(
      @AuthenticationPrincipal AuthPrincipal principal,
      @PathVariable Long threadId
  ) {
    return service.listMessages(requireUserId(principal), threadId);
  }

  @PostMapping("/threads/{threadId}/messages")
  public MessageItemResponse sendMessage(
      @AuthenticationPrincipal AuthPrincipal principal,
      @PathVariable Long threadId,
      @RequestBody MessageCreateRequest request
  ) {
    return service.sendMessage(requireUserId(principal), threadId, request);
  }

  @PostMapping("/direct")
  public ResponseEntity<DirectThreadResponse> createDirectThread(
      @AuthenticationPrincipal AuthPrincipal principal,
      @RequestBody DirectThreadRequest request
  ) {
    WorkspaceMessageService.DirectThreadCreationResult result =
        service.getOrCreateDirectThread(requireUserId(principal), request);
    HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
    return ResponseEntity.status(status).body(result.response());
  }

  private Long requireUserId(AuthPrincipal principal) {
    if (principal == null || principal.getUserId() == null || principal.getUserId() <= 0) {
      throw new ValidationException("Authenticated user is required");
    }
    return principal.getUserId();
  }
}
