package com.phoenixtask.issues;

import com.phoenixtask.issues.model.Issue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@Validated
public class IssueController {
    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public ResponseEntity<List<Issue>> getIssues(@RequestParam @NotNull @Min(1) Long projectId) {
        return ResponseEntity.ok(issueService.getIssuesByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssue(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public ResponseEntity<Issue> createIssue(@Valid @RequestBody IssueRequests.CreateIssueRequest request) {
        Issue issue = new Issue();
        issue.setProjectId(request.projectId());
        issue.setTitle(request.title());
        issue.setDescription(request.description());
        issue.setReporterUserId(request.reporterUserId());
        issue.setAssigneeUserId(request.assigneeUserId());
        issue.setStatus(request.status());
        issue.setPriority(request.priority());
        issue.setPlannedStartDate(request.plannedStartDate());
        issue.setDueDate(request.dueDate());
        return ResponseEntity.ok(issueService.createIssue(issue));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public ResponseEntity<Issue> updateIssue(@PathVariable @Min(1) Long id, @Valid @RequestBody IssueRequests.UpdateIssueRequest request) {
        Issue issue = new Issue();
        issue.setTitle(request.title());
        issue.setDescription(request.description());
        issue.setAssigneeUserId(request.assigneeUserId());
        issue.setStatus(request.status());
        issue.setPriority(request.priority());
        issue.setPlannedStartDate(request.plannedStartDate());
        issue.setDueDate(request.dueDate());
        return ResponseEntity.ok(issueService.updateIssue(id, issue));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public ResponseEntity<Void> updateStatus(@PathVariable @Min(1) Long id, @Valid @RequestBody IssueRequests.UpdateStatusRequest request) {
        issueService.updateStatus(id, request.status());
        return ResponseEntity.noContent().build();
    }
}
