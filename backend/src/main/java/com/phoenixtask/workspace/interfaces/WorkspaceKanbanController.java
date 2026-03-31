package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceKanbanService;
import com.phoenixtask.workspace.application.dto.KanbanBoardResponse;
import com.phoenixtask.workspace.application.dto.KanbanProjectSummaryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/kanban")
public class WorkspaceKanbanController {

  private final WorkspaceKanbanService kanbanService;

  public WorkspaceKanbanController(WorkspaceKanbanService kanbanService) {
    this.kanbanService = kanbanService;
  }

  @GetMapping("/projects")
  public List<KanbanProjectSummaryResponse> listProjects() {
    return kanbanService.listProjects();
  }

  @GetMapping("/projects/{projectId}")
  public KanbanBoardResponse getBoard(@PathVariable Long projectId) {
    return kanbanService.getBoard(projectId);
  }
}
