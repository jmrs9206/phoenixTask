package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.WorkspaceGanttService;
import com.phoenixtask.workspace.application.dto.GanttBaselineResponse;
import com.phoenixtask.workspace.application.dto.GanttDependencyResponse;
import com.phoenixtask.workspace.application.dto.GanttProjectDetailResponse;
import com.phoenixtask.workspace.application.dto.GanttProjectResponse;
import com.phoenixtask.workspace.interfaces.dto.GanttDependencyCreateRequest;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/gantt")
public class WorkspaceGanttController {

  private final WorkspaceGanttService ganttService;

  public WorkspaceGanttController(WorkspaceGanttService ganttService) {
    this.ganttService = ganttService;
  }

  @GetMapping("/projects")
  public List<GanttProjectResponse> listProjects() {
    return ganttService.listProjects();
  }

  @GetMapping("/projects/{projectId}")
  public GanttProjectDetailResponse getProject(@PathVariable Long projectId) {
    return ganttService.getProject(projectId);
  }

  @PostMapping("/projects/{projectId}/baseline")
  public GanttBaselineResponse captureBaseline(
      @PathVariable Long projectId,
      @AuthenticationPrincipal AuthPrincipal principal
  ) {
    return ganttService.captureBaseline(projectId, requireUserId(principal));
  }

  @PostMapping("/projects/{projectId}/dependencies")
  public GanttDependencyResponse createDependency(
      @PathVariable Long projectId,
      @RequestBody GanttDependencyCreateRequest request,
      @AuthenticationPrincipal AuthPrincipal principal
  ) {
    return ganttService.createDependency(
        projectId,
        request.predecessorIssueId(),
        request.successorIssueId(),
        request.dependencyType(),
        requireUserId(principal)
    );
  }

  private Long requireUserId(AuthPrincipal principal) {
    if (principal == null || principal.getUserId() == null) {
      throw new ValidationException("Authenticated user is required");
    }
    return principal.getUserId();
  }
}
