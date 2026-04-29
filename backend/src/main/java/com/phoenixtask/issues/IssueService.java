package com.phoenixtask.issues;

import com.phoenixtask.iam.repository.UserRepository;
import com.phoenixtask.issues.model.Issue;
import com.phoenixtask.issues.repository.IssueRepository;
import com.phoenixtask.projects.model.Project;
import com.phoenixtask.projects.repository.ProjectRepository;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IssueService {
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public IssueService(IssueRepository issueRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<Issue> getIssuesByProject(Long projectId) {
        return issueRepository.findAllByProjectId(projectId);
    }

    public Issue getIssueById(Long id) {
        return issueRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
    }

    @Transactional
    public Issue createIssue(Issue issue) {
        Project project = projectRepository.findById(issue.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        userRepository.findById(issue.getReporterUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Reporter user not found"));

        if (issue.getAssigneeUserId() != null) {
            userRepository.findById(issue.getAssigneeUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found"));
        }

        int nextNumber = issueRepository.getNextIssueNumber(issue.getProjectId());
        issue.setIssueNumber(nextNumber);
        issue.setIssueKey(project.projectKey() + "-" + nextNumber);
        
        if (issue.getStatus() == null) issue.setStatus("BACKLOG");
        if (issue.getPriority() == null) issue.setPriority("LOW");

        return issueRepository.save(issue);
    }

    @Transactional
    public Issue updateIssue(Long id, Issue issueDetails) {
        Issue issue = getIssueById(id);
        
        issue.setTitle(issueDetails.getTitle());
        issue.setDescription(issueDetails.getDescription());
        issue.setStatus(issueDetails.getStatus());
        issue.setPriority(issueDetails.getPriority());
        issue.setAssigneeUserId(issueDetails.getAssigneeUserId());
        issue.setPlannedStartDate(issueDetails.getPlannedStartDate());
        issue.setDueDate(issueDetails.getDueDate());

        if (issue.getAssigneeUserId() != null) {
            userRepository.findById(issue.getAssigneeUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found"));
        }

        issueRepository.update(issue);
        return issue;
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        getIssueById(id); // Check existence
        issueRepository.updateStatus(id, status);
    }
}
