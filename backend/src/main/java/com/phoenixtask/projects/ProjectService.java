package com.phoenixtask.projects;

import com.phoenixtask.projects.model.Project;
import com.phoenixtask.projects.repository.ProjectRepository;
import com.phoenixtask.shared.error.ConflictException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public Project createProject(Project project) {
        if (projectRepository.findByKey(project.projectKey()).isPresent()) {
            throw new ConflictException("Project key already exists");
        }
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, Project updates) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        Project updated = new Project(
            existing.id(),
            updates.projectKey() != null ? updates.projectKey() : existing.projectKey(),
            updates.name() != null ? updates.name() : existing.name(),
            updates.description() != null ? updates.description() : existing.description(),
            existing.status(),
            updates.ownerUserId() != null ? updates.ownerUserId() : existing.ownerUserId(),
            updates.plannedStartDate() != null ? updates.plannedStartDate() : existing.plannedStartDate(),
            updates.plannedEndDate() != null ? updates.plannedEndDate() : existing.plannedEndDate(),
            existing.createdAt(),
            existing.updatedAt()
        );
        
        return projectRepository.save(updated);
    }

    public void updateProjectStatus(Long id, String status) {
        if (projectRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Project not found");
        }
        projectRepository.updateStatus(id, status);
    }
}
