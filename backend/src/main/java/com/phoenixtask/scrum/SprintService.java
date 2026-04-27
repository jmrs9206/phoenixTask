package com.phoenixtask.scrum;

import com.phoenixtask.issues.model.Issue;
import com.phoenixtask.issues.repository.IssueRepository;
import com.phoenixtask.projects.repository.ProjectRepository;
import com.phoenixtask.scrum.model.Sprint;
import com.phoenixtask.scrum.repository.SprintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SprintService {
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;

    public SprintService(SprintRepository sprintRepository, ProjectRepository projectRepository, IssueRepository issueRepository) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
    }

    public List<Sprint> getSprintsByProject(Long projectId) {
        return sprintRepository.findAllByProjectId(projectId);
    }

    public Sprint getSprintById(Long id) {
        return sprintRepository.findById(id).orElseThrow(() -> new RuntimeException("Sprint not found"));
    }

    @Transactional
    public Sprint createSprint(Sprint sprint) {
        if (sprint.getProjectId() == null || projectRepository.findById(sprint.getProjectId()).isEmpty()) {
            throw new RuntimeException("Valid Project ID is required");
        }
        if (sprint.getStartDate().isAfter(sprint.getEndDate())) {
            throw new RuntimeException("Start date must be before or equal to end date");
        }
        if (sprintRepository.existsByNameInProject(sprint.getProjectId(), sprint.getName())) {
            throw new RuntimeException("Sprint name already exists in this project");
        }

        sprint.setStatus("PLANNED");
        return sprintRepository.save(sprint);
    }

    @Transactional
    public Sprint updateSprint(Long id, Sprint sprintUpdates) {
        Sprint existing = getSprintById(id);
        
        if (sprintUpdates.getName() != null) {
            if (!existing.getName().equals(sprintUpdates.getName()) && 
                sprintRepository.existsByNameInProject(existing.getProjectId(), sprintUpdates.getName())) {
                throw new RuntimeException("Sprint name already exists in this project");
            }
            existing.setName(sprintUpdates.getName());
        }
        if (sprintUpdates.getGoal() != null) existing.setGoal(sprintUpdates.getGoal());
        if (sprintUpdates.getStartDate() != null) existing.setStartDate(sprintUpdates.getStartDate());
        if (sprintUpdates.getEndDate() != null) existing.setEndDate(sprintUpdates.getEndDate());

        if (existing.getStartDate().isAfter(existing.getEndDate())) {
            throw new RuntimeException("Start date must be before or equal to end date");
        }

        sprintRepository.update(existing);
        return existing;
    }

    @Transactional
    public void startSprint(Long id) {
        Sprint sprint = getSprintById(id);
        if (!"PLANNED".equals(sprint.getStatus())) {
            throw new RuntimeException("Only PLANNED sprints can be started");
        }
        if (sprintRepository.findActiveSprintByProjectId(sprint.getProjectId()).isPresent()) {
            throw new RuntimeException("There is already an ACTIVE sprint in this project");
        }
        sprintRepository.updateStatus(id, "ACTIVE");
    }

    @Transactional
    public void closeSprint(Long id) {
        Sprint sprint = getSprintById(id);
        if (!"ACTIVE".equals(sprint.getStatus())) {
            throw new RuntimeException("Only ACTIVE sprints can be closed");
        }
        sprintRepository.updateStatus(id, "CLOSED");
    }

    @Transactional
    public void assignIssue(Long sprintId, Long issueId) {
        Sprint sprint = getSprintById(sprintId);
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new RuntimeException("Issue not found"));

        if ("CLOSED".equals(sprint.getStatus())) {
            throw new RuntimeException("Cannot assign issues to a CLOSED sprint");
        }
        if (!sprint.getProjectId().equals(issue.getProjectId())) {
            throw new RuntimeException("Sprint and Issue must belong to the same project");
        }

        sprintRepository.updateIssueSprint(issueId, sprintId);
    }

    @Transactional
    public void unassignIssue(Long sprintId, Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new RuntimeException("Issue not found"));
        
        if (issue.getSprintId() == null || !issue.getSprintId().equals(sprintId)) {
            throw new RuntimeException("Issue does not belong to this sprint");
        }

        sprintRepository.updateIssueSprint(issueId, null);
    }
}
