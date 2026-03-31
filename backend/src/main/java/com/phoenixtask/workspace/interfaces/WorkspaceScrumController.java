package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceScrumService;
import com.phoenixtask.workspace.application.dto.IssueSummaryResponse;
import com.phoenixtask.workspace.application.dto.SprintCreateRequest;
import com.phoenixtask.workspace.application.dto.SprintHealthResponse;
import com.phoenixtask.workspace.application.dto.SprintIssueAssignRequest;
import com.phoenixtask.workspace.application.dto.SprintResponse;
import com.phoenixtask.workspace.application.dto.SprintUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/scrum")
public class WorkspaceScrumController {

  private final WorkspaceScrumService scrumService;

  public WorkspaceScrumController(WorkspaceScrumService scrumService) {
    this.scrumService = scrumService;
  }

  @GetMapping("/projects/{projectId}/sprints")
  public List<SprintResponse> listSprints(@PathVariable Long projectId) {
    return scrumService.listSprints(projectId);
  }

  @PostMapping("/projects/{projectId}/sprints")
  public SprintResponse createSprint(
      @PathVariable Long projectId,
      @Valid @RequestBody SprintCreateRequest request
  ) {
    return scrumService.createSprint(projectId, request);
  }

  @PostMapping("/sprints/{sprintId}")
  public SprintResponse updateSprint(
      @PathVariable Long sprintId,
      @Valid @RequestBody SprintUpdateRequest request
  ) {
    return scrumService.updateSprint(sprintId, request);
  }

  @GetMapping("/projects/{projectId}/backlog")
  public List<IssueSummaryResponse> listBacklog(@PathVariable Long projectId) {
    return scrumService.listBacklog(projectId);
  }

  @GetMapping("/sprints/{sprintId}/issues")
  public List<IssueSummaryResponse> listSprintIssues(@PathVariable Long sprintId) {
    return scrumService.listSprintIssues(sprintId);
  }

  @GetMapping("/sprints/{sprintId}/health")
  public SprintHealthResponse getSprintHealth(@PathVariable Long sprintId) {
    return scrumService.getSprintHealth(sprintId);
  }

  @PostMapping("/sprints/{sprintId}/issues")
  public void assignIssue(
      @PathVariable Long sprintId,
      @Valid @RequestBody SprintIssueAssignRequest request
  ) {
    scrumService.assignIssueToSprint(sprintId, request);
  }

  @PostMapping("/sprints/backlog/issues")
  public void moveIssueToBacklog(@Valid @RequestBody SprintIssueAssignRequest request) {
    scrumService.moveIssueToBacklog(request);
  }
}
