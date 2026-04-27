package com.phoenixtask.kanban;

import com.phoenixtask.kanban.model.KanbanBoard;
import com.phoenixtask.kanban.model.KanbanIssueCard;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kanban")
public class KanbanController {
    private final KanbanService kanbanService;

    public KanbanController(KanbanService kanbanService) {
        this.kanbanService = kanbanService;
    }

    @GetMapping("/board")
    public ResponseEntity<KanbanBoard> getBoard(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long sprintId) {
        return ResponseEntity.ok(kanbanService.getBoard(projectId, sprintId));
    }

    @PatchMapping("/issues/{issueId}/move")
    @PreAuthorize("hasAnyAuthority('platform_owner','manager','developer','qa')")
    public ResponseEntity<KanbanIssueCard> moveIssue(
            @PathVariable Long issueId,
            @RequestBody MoveRequest request) {
        return ResponseEntity.ok(kanbanService.moveIssue(
                issueId, 
                request.getProjectId(), 
                request.getTargetStatus(), 
                request.getTargetIndex()));
    }

    public static class MoveRequest {
        private Long projectId;
        private String targetStatus;
        private int targetIndex;

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getTargetStatus() { return targetStatus; }
        public void setTargetStatus(String targetStatus) { this.targetStatus = targetStatus; }
        public int getTargetIndex() { return targetIndex; }
        public void setTargetIndex(int targetIndex) { this.targetIndex = targetIndex; }
    }
}
