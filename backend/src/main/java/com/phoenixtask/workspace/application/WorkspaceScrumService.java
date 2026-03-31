package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ConflictException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.IssueSummaryResponse;
import com.phoenixtask.workspace.application.dto.SprintCreateRequest;
import com.phoenixtask.workspace.application.dto.SprintHealthResponse;
import com.phoenixtask.workspace.application.dto.SprintIssueAssignRequest;
import com.phoenixtask.workspace.application.dto.SprintResponse;
import com.phoenixtask.workspace.application.dto.SprintUpdateRequest;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceProjectRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceSprintRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceSprintMetricsRepository;
import com.phoenixtask.workspace.application.dto.SprintBurndownPointResponse;
import com.phoenixtask.workspace.application.dto.SprintCommitmentResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceScrumService {

  private static final Set<String> SPRINT_STATUSES = Set.of("PLANNED", "ACTIVE", "COMPLETED");

  private final WorkspaceSprintRepository sprintRepository;
  private final WorkspaceProjectRepository projectRepository;
  private final WorkspaceIssueRepository issueRepository;
  private final WorkspaceSprintMetricsRepository sprintMetricsRepository;

  public WorkspaceScrumService(
      WorkspaceSprintRepository sprintRepository,
      WorkspaceProjectRepository projectRepository,
      WorkspaceIssueRepository issueRepository,
      WorkspaceSprintMetricsRepository sprintMetricsRepository
  ) {
    this.sprintRepository = sprintRepository;
    this.projectRepository = projectRepository;
    this.issueRepository = issueRepository;
    this.sprintMetricsRepository = sprintMetricsRepository;
  }

  public List<SprintResponse> listSprints(Long projectId) {
    ensureProjectExists(projectId);
    return sprintRepository.findByProject(projectId).stream()
        .map(this::toSprintResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public SprintResponse createSprint(Long projectId, SprintCreateRequest request) {
    ensureProjectExists(projectId);
    String status = normalizeStatus(request.status());
    if (!SPRINT_STATUSES.contains(status)) {
      throw new ValidationException("Invalid sprint status");
    }
    if (request.startDate().isAfter(request.endDate())) {
      throw new ValidationException("Sprint start date must be before end date");
    }
    if ("ACTIVE".equals(status) && sprintRepository.hasActiveSprint(projectId)) {
      throw new ConflictException("Another ACTIVE sprint already exists for this project");
    }
    String name = request.name().trim();
    if (name.isBlank()) {
      throw new ValidationException("Sprint name is required");
    }
    String goal = normalizeGoal(request.goal());
    Long id = sprintRepository.insertSprint(projectId, name, goal, status, request.startDate(), request.endDate());
    WorkspaceSprintRepository.SprintRow row = sprintRepository.findById(id)
        .orElseThrow(() -> new ConflictException("Sprint could not be created"));
    if ("ACTIVE".equals(row.status())) {
      ensureCommitmentSnapshot(row);
    }
    return toSprintResponse(row);
  }

  @Transactional
  public SprintResponse updateSprint(Long sprintId, SprintUpdateRequest request) {
    WorkspaceSprintRepository.SprintRow sprint = ensureSprintExists(sprintId);
    String newStatus = request.status() == null ? sprint.status() : normalizeStatus(request.status());
    if (!SPRINT_STATUSES.contains(newStatus)) {
      throw new ValidationException("Invalid sprint status");
    }
    if (!isStatusTransitionAllowed(sprint.status(), newStatus)) {
      throw new ConflictException("Invalid sprint status transition");
    }
    if ("ACTIVE".equals(newStatus) && !"ACTIVE".equals(sprint.status())
        && sprintRepository.hasActiveSprint(sprint.projectId())) {
      throw new ConflictException("Another ACTIVE sprint already exists for this project");
    }
    String goal = request.goal() == null ? sprint.goal() : normalizeGoal(request.goal());
    sprintRepository.updateSprint(sprintId, goal, newStatus);
    WorkspaceSprintRepository.SprintRow updated = sprintRepository.findById(sprintId)
        .orElseThrow(() -> new ConflictException("Sprint could not be updated"));
    if ("ACTIVE".equals(updated.status())) {
      ensureCommitmentSnapshot(updated);
    }
    return toSprintResponse(updated);
  }

  public SprintHealthResponse getSprintHealth(Long sprintId) {
    WorkspaceSprintRepository.SprintRow sprint = ensureSprintExists(sprintId);
    WorkspaceSprintMetricsRepository.CommitmentSnapshotRow snapshot = ensureCommitmentSnapshotIfActive(sprint);

    int committed = snapshot == null ? 0 : snapshot.committedCount();
    int completed = snapshot == null ? 0 : sprintMetricsRepository.countCompletedCommitted(sprintId);
    int remaining = Math.max(committed - completed, 0);

    if (snapshot != null) {
      LocalDate today = LocalDate.now();
      sprintMetricsRepository.insertBurndownPoint(sprintId, today, committed, remaining);
    }

    List<SprintBurndownPointResponse> burndown = sprintMetricsRepository.findBurndownPoints(sprintId).stream()
        .map(point -> new SprintBurndownPointResponse(
            point.pointDate(),
            point.committedCount(),
            point.remainingCount()
        ))
        .toList();

    Double sayDo = committed == 0 ? null : (completed * 100.0) / committed;
    String health = resolveHealthStatus(sprint, committed, completed);

    SprintCommitmentResponse commitment = snapshot == null ? null : new SprintCommitmentResponse(
        snapshot.capturedAt(),
        committed,
        completed,
        remaining
    );

    return new SprintHealthResponse(
        sprint.id(),
        sprint.projectId(),
        sprint.name(),
        sprint.goal(),
        sprint.status(),
        sprint.startDate(),
        sprint.endDate(),
        health,
        sayDo,
        commitment,
        burndown
    );
  }

  public List<IssueSummaryResponse> listBacklog(Long projectId) {
    ensureProjectExists(projectId);
    return issueRepository.findBacklogByProject(projectId).stream()
        .map(this::toIssueSummary)
        .collect(Collectors.toList());
  }

  public List<IssueSummaryResponse> listSprintIssues(Long sprintId) {
    ensureSprintExists(sprintId);
    return issueRepository.findBySprint(sprintId).stream()
        .map(this::toIssueSummary)
        .collect(Collectors.toList());
  }

  @Transactional
  public void assignIssueToSprint(Long sprintId, SprintIssueAssignRequest request) {
    WorkspaceSprintRepository.SprintRow sprint = ensureSprintExists(sprintId);
    if ("COMPLETED".equalsIgnoreCase(sprint.status())) {
      throw new ConflictException("Cannot assign issues to a COMPLETED sprint");
    }
    Long issueId = request.issueId();
    if (!issueRepository.existsById(issueId)) {
      throw new ResourceNotFoundException("Issue not found");
    }
    Long issueProjectId = issueRepository.findProjectId(issueId);
    if (issueProjectId == null || !issueProjectId.equals(sprint.projectId())) {
      throw new ConflictException("Issue does not belong to the sprint project");
    }
    issueRepository.assignIssueToSprint(issueId, sprintId);
  }

  @Transactional
  public void moveIssueToBacklog(SprintIssueAssignRequest request) {
    Long issueId = request.issueId();
    if (!issueRepository.existsById(issueId)) {
      throw new ResourceNotFoundException("Issue not found");
    }
    issueRepository.moveIssueToBacklog(issueId);
  }

  private void ensureProjectExists(Long projectId) {
    if (!projectRepository.existsById(projectId)) {
      throw new ResourceNotFoundException("Project not found");
    }
  }

  private WorkspaceSprintRepository.SprintRow ensureSprintExists(Long sprintId) {
    return sprintRepository.findById(sprintId)
        .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
  }

  private SprintResponse toSprintResponse(WorkspaceSprintRepository.SprintRow row) {
    return new SprintResponse(
        row.id(),
        row.projectId(),
        row.name(),
        row.goal(),
        row.status(),
        row.startDate(),
        row.endDate(),
        row.createdAt(),
        row.updatedAt()
    );
  }

  private IssueSummaryResponse toIssueSummary(WorkspaceIssueRepository.IssueSummaryRow row) {
    return new IssueSummaryResponse(
        row.id(),
        row.issueKey(),
        row.title(),
        row.status(),
        row.priority(),
        row.assigneeName()
    );
  }

  private String normalizeGoal(String goal) {
    if (goal == null || goal.isBlank()) {
      return null;
    }
    return goal.trim();
  }

  private String normalizeStatus(String status) {
    return status == null ? "" : status.trim().toUpperCase();
  }

  private boolean isStatusTransitionAllowed(String current, String next) {
    if (current.equals(next)) {
      return true;
    }
    if ("PLANNED".equals(current) && "ACTIVE".equals(next)) {
      return true;
    }
    if ("ACTIVE".equals(current) && "COMPLETED".equals(next)) {
      return true;
    }
    return false;
  }

  private WorkspaceSprintMetricsRepository.CommitmentSnapshotRow ensureCommitmentSnapshotIfActive(
      WorkspaceSprintRepository.SprintRow sprint
  ) {
    if (!"ACTIVE".equals(sprint.status())) {
      return sprintMetricsRepository.findSnapshot(sprint.id()).orElse(null);
    }
    return ensureCommitmentSnapshot(sprint);
  }

  private WorkspaceSprintMetricsRepository.CommitmentSnapshotRow ensureCommitmentSnapshot(
      WorkspaceSprintRepository.SprintRow sprint
  ) {
    return sprintMetricsRepository.findSnapshot(sprint.id()).orElseGet(() -> {
      List<Long> issueIds = issueRepository.findIssueIdsBySprint(sprint.id());
      WorkspaceSprintMetricsRepository.CommitmentSnapshotRow snapshot =
          sprintMetricsRepository.createSnapshot(sprint.id(), issueIds.size());
      sprintMetricsRepository.insertCommitments(sprint.id(), issueIds, snapshot.capturedAt());
      sprintMetricsRepository.insertBurndownPoint(
          sprint.id(),
          snapshot.capturedAt().toLocalDate(),
          snapshot.committedCount(),
          Math.max(snapshot.committedCount() - sprintMetricsRepository.countCompletedCommitted(sprint.id()), 0)
      );
      return snapshot;
    });
  }

  private String resolveHealthStatus(
      WorkspaceSprintRepository.SprintRow sprint,
      int committed,
      int completed
  ) {
    if (!"ACTIVE".equals(sprint.status())) {
      return sprint.status();
    }
    if (committed == 0) {
      return "NO_COMMITMENT";
    }
    long totalDays = ChronoUnit.DAYS.between(sprint.startDate(), sprint.endDate());
    if (totalDays <= 0) {
      return "ON_TRACK";
    }
    long elapsedDays = ChronoUnit.DAYS.between(sprint.startDate(), LocalDate.now());
    double timeProgress = Math.min(Math.max(elapsedDays / (double) totalDays, 0.0), 1.0);
    double completion = completed / (double) committed;
    return completion >= timeProgress ? "ON_TRACK" : "AT_RISK";
  }
}
