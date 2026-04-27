package com.phoenixtask.issues;

import com.phoenixtask.issues.model.Issue;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public ResponseEntity<List<Issue>> getIssues(@RequestParam Long projectId) {
        return ResponseEntity.ok(issueService.getIssuesByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssue(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public ResponseEntity<Issue> createIssue(@RequestBody Issue issue) {
        return ResponseEntity.ok(issueService.createIssue(issue));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public ResponseEntity<Issue> updateIssue(@PathVariable Long id, @RequestBody Issue issue) {
        return ResponseEntity.ok(issueService.updateIssue(id, issue));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        issueService.updateStatus(id, body.get("status"));
        return ResponseEntity.noContent().build();
    }
}
