package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.OkrCheckinCreateRequest;
import com.phoenixtask.workspace.application.dto.OkrCheckinResponse;
import com.phoenixtask.workspace.application.dto.OkrKeyResultCreateRequest;
import com.phoenixtask.workspace.application.dto.OkrKeyResultResponse;
import com.phoenixtask.workspace.application.dto.OkrInitiativeCreateRequest;
import com.phoenixtask.workspace.application.dto.OkrInitiativeResponse;
import com.phoenixtask.workspace.application.dto.OkrObjectiveCloseRequest;
import com.phoenixtask.workspace.application.dto.OkrObjectiveCreateRequest;
import com.phoenixtask.workspace.application.dto.OkrObjectiveDetailResponse;
import com.phoenixtask.workspace.application.dto.OkrObjectiveResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceOkrRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceProjectRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceUserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceOkrService {

  private static final Set<String> OBJECTIVE_STATUSES = Set.of("DRAFT", "ACTIVE", "COMPLETED", "CANCELLED");
  private static final Set<String> KEY_RESULT_STATUSES = Set.of("ON_TRACK", "AT_RISK", "OFF_TRACK", "COMPLETED");
  private static final Set<String> CONFIDENCE_LEVELS = Set.of("LOW", "MEDIUM", "HIGH");

  private final WorkspaceOkrRepository okrRepository;
  private final WorkspaceUserRepository userRepository;
  private final WorkspaceProjectRepository projectRepository;
  private final WorkspaceIssueRepository issueRepository;

  public WorkspaceOkrService(
      WorkspaceOkrRepository okrRepository,
      WorkspaceUserRepository userRepository,
      WorkspaceProjectRepository projectRepository,
      WorkspaceIssueRepository issueRepository
  ) {
    this.okrRepository = okrRepository;
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.issueRepository = issueRepository;
  }

  public List<OkrObjectiveResponse> listObjectives(Long projectId) {
    ensureProjectExists(projectId);
    Map<Long, Double> progressMap = okrRepository.fetchProgressByObjective();
    return okrRepository.findObjectives(projectId).stream()
        .map(row -> new OkrObjectiveResponse(
            row.id(),
            row.projectId(),
            row.projectKey(),
            row.projectName(),
            row.title(),
            row.description(),
            row.status(),
            row.periodStart(),
            row.periodEnd(),
            row.ownerUserId(),
            row.ownerName(),
            clampProgress(progressMap.get(row.id())),
            row.confidenceLevel(),
            row.finalScore(),
            row.closedAt()
        ))
        .collect(Collectors.toList());
  }

  public List<OkrObjectiveDetailResponse> listObjectiveDetails(Long projectId) {
    ensureProjectExists(projectId);
    Map<Long, Double> progressMap = okrRepository.fetchProgressByObjective();
    return okrRepository.findObjectives(projectId).stream()
        .map(row -> {
          List<OkrKeyResultResponse> keyResults = okrRepository.findKeyResultsByObjective(row.id()).stream()
              .map(this::toKeyResultResponse)
              .collect(Collectors.toList());
          List<OkrCheckinResponse> checkins = okrRepository.findCheckinsByObjective(row.id()).stream()
              .map(this::toCheckinResponse)
              .collect(Collectors.toList());
          List<OkrInitiativeResponse> initiatives = okrRepository.findInitiativesByObjective(row.id()).stream()
              .map(this::toInitiativeResponse)
              .collect(Collectors.toList());
          return new OkrObjectiveDetailResponse(
              row.id(),
              row.projectId(),
              row.projectKey(),
              row.projectName(),
              row.title(),
              row.description(),
              row.status(),
              row.periodStart(),
              row.periodEnd(),
              row.ownerUserId(),
              row.ownerName(),
              clampProgress(progressMap.get(row.id())),
              keyResults,
              row.confidenceLevel(),
              row.finalScore(),
              row.closedAt(),
              checkins,
              initiatives
          );
        })
        .collect(Collectors.toList());
  }

  public OkrObjectiveDetailResponse getObjective(Long objectiveId) {
    WorkspaceOkrRepository.ObjectiveRow row = okrRepository.findObjectiveById(objectiveId)
        .orElseThrow(() -> new ResourceNotFoundException("Objective not found"));
    List<OkrKeyResultResponse> keyResults = okrRepository.findKeyResultsByObjective(objectiveId).stream()
        .map(this::toKeyResultResponse)
        .collect(Collectors.toList());
    List<OkrCheckinResponse> checkins = okrRepository.findCheckinsByObjective(objectiveId).stream()
        .map(this::toCheckinResponse)
        .collect(Collectors.toList());
    List<OkrInitiativeResponse> initiatives = okrRepository.findInitiativesByObjective(objectiveId).stream()
        .map(this::toInitiativeResponse)
        .collect(Collectors.toList());
    double progress = clampProgress(okrRepository.fetchProgressByObjective().get(objectiveId));
    return new OkrObjectiveDetailResponse(
        row.id(),
        row.projectId(),
        row.projectKey(),
        row.projectName(),
        row.title(),
        row.description(),
        row.status(),
        row.periodStart(),
        row.periodEnd(),
        row.ownerUserId(),
        row.ownerName(),
        progress,
        keyResults,
        row.confidenceLevel(),
        row.finalScore(),
        row.closedAt(),
        checkins,
        initiatives
    );
  }

  @Transactional
  public OkrObjectiveResponse createObjective(Long projectId, OkrObjectiveCreateRequest request) {
    String status = normalizeStatus(request.status());
    if (!OBJECTIVE_STATUSES.contains(status)) {
      throw new ValidationException("Invalid objective status");
    }
    if (request.periodStart().isAfter(request.periodEnd())) {
      throw new ValidationException("Objective period start must be before end date");
    }
    if (!userRepository.existsById(request.ownerUserId())) {
      throw new ResourceNotFoundException("Owner user not found");
    }
    ensureProjectExists(projectId);
    Long id = okrRepository.insertObjective(
        projectId,
        request.ownerUserId(),
        request.title().trim(),
        normalizeDescription(request.description()),
        status,
        request.periodStart(),
        request.periodEnd()
    );
    WorkspaceOkrRepository.ObjectiveRow row = okrRepository.findObjectiveById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Objective not found"));
    return new OkrObjectiveResponse(
        row.id(),
        row.projectId(),
        row.projectKey(),
        row.projectName(),
        row.title(),
        row.description(),
        row.status(),
        row.periodStart(),
        row.periodEnd(),
        row.ownerUserId(),
        row.ownerName(),
        0.0,
        row.confidenceLevel(),
        row.finalScore(),
        row.closedAt()
    );
  }

  @Transactional
  public OkrCheckinResponse addCheckin(Long objectiveId, Long authorUserId, OkrCheckinCreateRequest request) {
    ensureObjectiveExists(objectiveId);
    if (!userRepository.existsById(authorUserId)) {
      throw new ResourceNotFoundException("User not found");
    }
    String confidenceLevel = normalizeStatus(request.confidenceLevel());
    if (!CONFIDENCE_LEVELS.contains(confidenceLevel)) {
      throw new ValidationException("Invalid confidence level");
    }
    if (request.progressPercent() != null && (request.progressPercent() < 0 || request.progressPercent() > 100)) {
      throw new ValidationException("Progress must be between 0 and 100");
    }
    Long checkinId = okrRepository.insertCheckin(
        objectiveId,
        authorUserId,
        request.progressPercent(),
        confidenceLevel,
        normalizeDescription(request.note())
    );
    okrRepository.updateObjectiveConfidence(objectiveId, confidenceLevel);
    return okrRepository.findCheckinsByObjective(objectiveId).stream()
        .filter(row -> row.id().equals(checkinId))
        .findFirst()
        .map(this::toCheckinResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Check-in not found"));
  }

  @Transactional
  public OkrObjectiveResponse closeObjective(Long objectiveId, OkrObjectiveCloseRequest request) {
    WorkspaceOkrRepository.ObjectiveRow row = okrRepository.findObjectiveById(objectiveId)
        .orElseThrow(() -> new ResourceNotFoundException("Objective not found"));
    String status = normalizeStatus(request.status());
    if (!Set.of("COMPLETED", "CANCELLED").contains(status)) {
      throw new ValidationException("Invalid close status");
    }
    okrRepository.closeObjective(objectiveId, status, BigDecimal.valueOf(request.finalScore()));
    WorkspaceOkrRepository.ObjectiveRow updated = okrRepository.findObjectiveById(objectiveId)
        .orElseThrow(() -> new ResourceNotFoundException("Objective not found"));
    return new OkrObjectiveResponse(
        updated.id(),
        updated.projectId(),
        updated.projectKey(),
        updated.projectName(),
        updated.title(),
        updated.description(),
        updated.status(),
        updated.periodStart(),
        updated.periodEnd(),
        updated.ownerUserId(),
        updated.ownerName(),
        clampProgress(okrRepository.fetchProgressByObjective().get(updated.id())),
        updated.confidenceLevel(),
        updated.finalScore(),
        updated.closedAt()
    );
  }

  @Transactional
  public OkrInitiativeResponse addInitiative(Long objectiveId, OkrInitiativeCreateRequest request) {
    WorkspaceOkrRepository.ObjectiveRow objective = okrRepository.findObjectiveById(objectiveId)
        .orElseThrow(() -> new ResourceNotFoundException("Objective not found"));
    Long issueId = request.issueId();
    if (issueId == null || !issueRepository.existsById(issueId)) {
      throw new ResourceNotFoundException("Issue not found");
    }
    Long issueProjectId = issueRepository.findProjectId(issueId);
    if (issueProjectId == null || !issueProjectId.equals(objective.projectId())) {
      throw new ValidationException("Issue does not belong to this project");
    }
    Long id = okrRepository.insertInitiative(objectiveId, issueId);
    return okrRepository.findInitiativesByObjective(objectiveId).stream()
        .filter(row -> row.id().equals(id))
        .findFirst()
        .map(this::toInitiativeResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Initiative not found"));
  }

  @Transactional
  public OkrKeyResultResponse addKeyResult(Long objectiveId, OkrKeyResultCreateRequest request) {
    WorkspaceOkrRepository.ObjectiveRow objective = okrRepository.findObjectiveById(objectiveId)
        .orElseThrow(() -> new ResourceNotFoundException("Objective not found"));
    String status = normalizeStatus(request.status());
    if (!KEY_RESULT_STATUSES.contains(status)) {
      throw new ValidationException("Invalid key result status");
    }
    BigDecimal target = request.targetValue();
    BigDecimal current = request.currentValue();
    if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
      throw new ValidationException("Target value must be greater than zero");
    }
    if (current == null || current.compareTo(BigDecimal.ZERO) < 0) {
      throw new ValidationException("Current value must be zero or higher");
    }
    Long keyResultId = okrRepository.insertKeyResult(
        objectiveId,
        objective.projectId(),
        request.title().trim(),
        target,
        current,
        request.unit().trim(),
        status
    );
    if (keyResultId == null) {
      throw new ResourceNotFoundException("Key result not found");
    }
    return okrRepository.findKeyResultById(keyResultId)
        .map(this::toKeyResultResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Key result not found"));
  }

  private void ensureProjectExists(Long projectId) {
    if (projectId == null || !projectRepository.existsById(projectId)) {
      throw new ResourceNotFoundException("Project not found");
    }
  }

  private void ensureObjectiveExists(Long objectiveId) {
    if (okrRepository.findObjectiveById(objectiveId).isEmpty()) {
      throw new ResourceNotFoundException("Objective not found");
    }
  }

  private OkrKeyResultResponse toKeyResultResponse(WorkspaceOkrRepository.KeyResultRow row) {
    return new OkrKeyResultResponse(
        row.id(),
        row.objectiveId(),
        row.projectId(),
        row.projectKey(),
        row.projectName(),
        row.title(),
        row.targetValue(),
        row.currentValue(),
        row.unit(),
        row.status()
    );
  }

  private OkrCheckinResponse toCheckinResponse(WorkspaceOkrRepository.CheckinRow row) {
    return new OkrCheckinResponse(
        row.id(),
        row.authorUserId(),
        row.authorName(),
        row.progressPercent(),
        row.confidenceLevel(),
        row.note(),
        row.createdAt()
    );
  }

  private OkrInitiativeResponse toInitiativeResponse(WorkspaceOkrRepository.InitiativeRow row) {
    return new OkrInitiativeResponse(
        row.id(),
        row.issueId(),
        row.issueKey(),
        row.issueTitle(),
        row.projectId(),
        row.projectKey(),
        row.projectName()
    );
  }

  private String normalizeStatus(String status) {
    return status == null ? "" : status.trim().toUpperCase();
  }

  private String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return description.trim();
  }

  private double clampProgress(Double value) {
    if (value == null) {
      return 0.0;
    }
    if (value < 0) {
      return 0.0;
    }
    if (value > 1) {
      return 1.0;
    }
    return value;
  }
}
