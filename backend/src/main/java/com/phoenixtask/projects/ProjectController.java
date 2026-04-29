package com.phoenixtask.projects;

import com.phoenixtask.projects.model.Project;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> listProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public Project getProject(@PathVariable @Min(1) Long id) {
        return projectService.getProjectById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public Project createProject(@Valid @RequestBody ProjectRequests.CreateProjectRequest request) {
        Project project = new Project(
            null, request.projectKey(), request.name(), request.description(), 
            "PLANNED", request.ownerUserId(), request.plannedStartDate(), 
            request.plannedEndDate(), null, null
        );
        return projectService.createProject(project);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public Project updateProject(@PathVariable @Min(1) Long id, @Valid @RequestBody ProjectRequests.UpdateProjectRequest request) {
        Project updates = new Project(
            null, request.projectKey(), request.name(), request.description(), 
            null, request.ownerUserId(), request.plannedStartDate(), 
            request.plannedEndDate(), null, null
        );
        return projectService.updateProject(id, updates);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('platform_owner', 'manager')")
    public void updateStatus(@PathVariable @Min(1) Long id, @Valid @RequestBody ProjectRequests.UpdateStatusRequest request) {
        projectService.updateProjectStatus(id, request.status());
    }
}
