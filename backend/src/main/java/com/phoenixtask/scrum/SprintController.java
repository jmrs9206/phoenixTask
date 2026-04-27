package com.phoenixtask.scrum;

import com.phoenixtask.scrum.model.Sprint;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrum/sprints")
public class SprintController {
    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Sprint> list(@RequestParam Long projectId) {
        return sprintService.getSprintsByProject(projectId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Sprint get(@PathVariable Long id) {
        return sprintService.getSprintById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public Sprint create(@RequestBody Sprint sprint) {
        return sprintService.createSprint(sprint);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public Sprint update(@PathVariable Long id, @RequestBody Sprint sprint) {
        return sprintService.updateSprint(id, sprint);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public void start(@PathVariable Long id) {
        sprintService.startSprint(id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public void close(@PathVariable Long id) {
        sprintService.closeSprint(id);
    }

    @PostMapping("/{id}/issues/{issueId}")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public void assignIssue(@PathVariable Long id, @PathVariable Long issueId) {
        sprintService.assignIssue(id, issueId);
    }

    @DeleteMapping("/{id}/issues/{issueId}")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public void unassignIssue(@PathVariable Long id, @PathVariable Long issueId) {
        sprintService.unassignIssue(id, issueId);
    }
}
