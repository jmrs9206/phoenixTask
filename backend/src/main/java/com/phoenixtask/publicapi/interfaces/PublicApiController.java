package com.phoenixtask.publicapi.interfaces;

import com.phoenixtask.publicapi.application.PublicApiQueryService;
import com.phoenixtask.publicapi.interfaces.dto.PublicApiIssueDetailResponse;
import com.phoenixtask.publicapi.interfaces.dto.PublicApiIssueResponse;
import com.phoenixtask.publicapi.interfaces.dto.PublicApiProjectDetailResponse;
import com.phoenixtask.publicapi.interfaces.dto.PublicApiProjectResponse;
import com.phoenixtask.shared.interfaces.PageRequest;
import com.phoenixtask.shared.interfaces.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicApiController {

  private final PublicApiQueryService queryService;

  public PublicApiController(PublicApiQueryService queryService) {
    this.queryService = queryService;
  }

  @GetMapping("/projects")
  public PageResponse<PublicApiProjectResponse> projects(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String query
  ) {
    PageRequest pageRequest = PageRequest.of(page, pageSize);
    return queryService.listProjects(pageRequest, status, query);
  }

  @GetMapping("/projects/{projectId}")
  public PublicApiProjectDetailResponse project(@PathVariable Long projectId) {
    return queryService.getProject(projectId);
  }

  @GetMapping("/issues")
  public PageResponse<PublicApiIssueResponse> issues(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) Long projectId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) String query
  ) {
    PageRequest pageRequest = PageRequest.of(page, pageSize);
    return queryService.listIssues(pageRequest, projectId, status, priority, query);
  }

  @GetMapping("/issues/{issueId}")
  public PublicApiIssueDetailResponse issue(@PathVariable Long issueId) {
    return queryService.getIssue(issueId);
  }
}
