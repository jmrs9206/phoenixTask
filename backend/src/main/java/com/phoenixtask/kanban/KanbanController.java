package com.phoenixtask.kanban;

import com.phoenixtask.kanban.model.KanbanBoard;
import com.phoenixtask.kanban.model.KanbanIssueCard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kanban")
@Validated
public class KanbanController {
    private final KanbanService kanbanService;

    public KanbanController(KanbanService kanbanService) {
        this.kanbanService = kanbanService;
    }

    @GetMapping("/board")
    public ResponseEntity<KanbanBoard> getBoard(
            @RequestParam @Min(1) Long projectId,
            @RequestParam(required = false) @Min(1) Long sprintId) {
        return ResponseEntity.ok(kanbanService.getBoard(projectId, sprintId));
    }

    @PatchMapping("/issues/{issueId}/move")
    @PreAuthorize("hasAnyAuthority('platform_owner','manager','developer','qa')")
    public ResponseEntity<KanbanIssueCard> moveIssue(
            @PathVariable @Min(1) Long issueId,
            @RequestBody @Valid KanbanRequests.MoveRequest request) {
        return ResponseEntity.ok(kanbanService.moveIssue(
                issueId, 
                request.projectId(), 
                request.targetStatus(), 
                request.targetIndex()));
    }
}
