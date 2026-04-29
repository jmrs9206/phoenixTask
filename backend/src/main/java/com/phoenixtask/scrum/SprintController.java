package com.phoenixtask.scrum;

import com.phoenixtask.scrum.model.Sprint;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrum/sprints")
@Validated
public class SprintController {
    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Sprint> list(@RequestParam @NotNull @Min(1) Long projectId) {
        return sprintService.getSprintsByProject(projectId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Sprint get(@PathVariable @Min(1) Long id) {
        return sprintService.getSprintById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public Sprint create(@Valid @RequestBody SprintRequests.CreateSprintRequest request) {
        Sprint sprint = new Sprint();
        sprint.setProjectId(request.projectId());
        sprint.setName(request.name());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setStatus(request.status() != null ? request.status() : "PLANNED");
        return sprintService.createSprint(sprint);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public Sprint update(@PathVariable @Min(1) Long id, @Valid @RequestBody SprintRequests.UpdateSprintRequest request) {
        Sprint sprint = new Sprint();
        sprint.setName(request.name());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setStatus(request.status());
        return sprintService.updateSprint(id, sprint);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public void start(@PathVariable @Min(1) Long id) {
        sprintService.startSprint(id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public void close(@PathVariable @Min(1) Long id) {
        sprintService.closeSprint(id);
    }

    @PostMapping("/{id}/issues/{issueId}")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public void assignIssue(@PathVariable @Min(1) Long id, @PathVariable @Min(1) Long issueId) {
        sprintService.assignIssue(id, issueId);
    }

    @DeleteMapping("/{id}/issues/{issueId}")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public void unassignIssue(@PathVariable @Min(1) Long id, @PathVariable @Min(1) Long issueId) {
        sprintService.unassignIssue(id, issueId);
    }
}
