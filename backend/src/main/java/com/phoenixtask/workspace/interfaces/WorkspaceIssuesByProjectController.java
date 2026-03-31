package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceIssueService;
import com.phoenixtask.workspace.application.dto.IssueSummaryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/projects")
public class WorkspaceIssuesByProjectController {

  private final WorkspaceIssueService issueService;

  public WorkspaceIssuesByProjectController(WorkspaceIssueService issueService) {
    this.issueService = issueService;
  }

  @GetMapping("/{projectId}/issues")
  public List<IssueSummaryResponse> listProjectIssues(@PathVariable Long projectId) {
    return issueService.listIssuesByProject(projectId);
  }
}
