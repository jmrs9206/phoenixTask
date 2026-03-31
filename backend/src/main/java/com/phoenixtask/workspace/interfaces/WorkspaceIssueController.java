package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceIssueCodeLinkService;
import com.phoenixtask.workspace.application.WorkspaceIssueService;
import com.phoenixtask.workspace.application.dto.IssueCodeLinkResponse;
import com.phoenixtask.workspace.application.dto.IssueCreateRequest;
import com.phoenixtask.workspace.application.dto.IssueCreateResponse;
import com.phoenixtask.workspace.application.dto.IssueDetailResponse;
import com.phoenixtask.workspace.application.dto.IssueListItemResponse;
import com.phoenixtask.workspace.application.dto.IssueListQuery;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.shared.interfaces.PageRequest;
import com.phoenixtask.shared.interfaces.PageResponse;

@RestController
@RequestMapping("/api/workspace/issues")
public class WorkspaceIssueController {

  private final WorkspaceIssueService service;
  private final WorkspaceIssueCodeLinkService codeLinkService;

  public WorkspaceIssueController(
      WorkspaceIssueService service,
      WorkspaceIssueCodeLinkService codeLinkService
  ) {
    this.service = service;
    this.codeLinkService = codeLinkService;
  }

  @GetMapping
  public PageResponse<IssueListItemResponse> listIssues(
      @AuthenticationPrincipal AuthPrincipal principal,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) Long projectId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) Long assigneeId,
      @RequestParam(required = false) String query
  ) {
    PageRequest pageRequest = PageRequest.of(page, pageSize);
    IssueListQuery listQuery = new IssueListQuery(projectId, status, priority, assigneeId, query);
    return service.listIssuesForUser(requireUserId(principal), listQuery, pageRequest);
  }

  @GetMapping("/{issueId}")
  public IssueDetailResponse getIssue(@PathVariable Long issueId) {
    return service.getIssue(issueId);
  }

  @GetMapping("/{issueId}/code-links")
  public List<IssueCodeLinkResponse> getIssueCodeLinks(@PathVariable Long issueId) {
    return codeLinkService.listByIssue(issueId);
  }

  @PostMapping
  public IssueCreateResponse createIssue(@Valid @RequestBody IssueCreateRequest request) {
    return service.createIssue(request);
  }

  private Long requireUserId(AuthPrincipal principal) {
    if (principal == null || principal.getUserId() == null || principal.getUserId() <= 0) {
      throw new ValidationException("Authenticated user is required");
    }
    return principal.getUserId();
  }
}
