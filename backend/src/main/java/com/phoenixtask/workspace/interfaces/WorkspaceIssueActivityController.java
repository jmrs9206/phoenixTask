package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceIssueActivityService;
import com.phoenixtask.workspace.application.dto.IssueActivityResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/issues/{issueId}/activity")
public class WorkspaceIssueActivityController {

  private final WorkspaceIssueActivityService service;

  public WorkspaceIssueActivityController(WorkspaceIssueActivityService service) {
    this.service = service;
  }

  @GetMapping
  public List<IssueActivityResponse> listActivity(@PathVariable Long issueId) {
    return service.listActivity(issueId);
  }
}
