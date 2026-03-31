package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.workspace.application.dto.KanbanBoardResponse;
import com.phoenixtask.workspace.application.dto.KanbanColumnSummaryResponse;
import com.phoenixtask.workspace.application.dto.KanbanFlowMetricsResponse;
import com.phoenixtask.workspace.application.dto.KanbanIssueCardResponse;
import com.phoenixtask.workspace.application.dto.KanbanLaneColumnResponse;
import com.phoenixtask.workspace.application.dto.KanbanProjectSummaryResponse;
import com.phoenixtask.workspace.application.dto.KanbanSwimlaneResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceKanbanRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceProjectRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceKanbanService {

  private static final List<String> STATUS_ORDER = List.of("OPEN", "IN_PROGRESS", "BLOCKED", "DONE");
  private static final Map<String, ColumnPolicyDefaults> DEFAULT_POLICIES = Map.of(
      "OPEN", new ColumnPolicyDefaults(12, "Clarify scope before pulling into progress."),
      "IN_PROGRESS", new ColumnPolicyDefaults(5, "Limit active work and keep focus tight."),
      "BLOCKED", new ColumnPolicyDefaults(3, "Escalate blockers within 24h."),
      "DONE", new ColumnPolicyDefaults(999, "Verify outcomes and close the loop.")
  );

  private final WorkspaceKanbanRepository kanbanRepository;
  private final WorkspaceProjectRepository projectRepository;

  public WorkspaceKanbanService(
      WorkspaceKanbanRepository kanbanRepository,
      WorkspaceProjectRepository projectRepository
  ) {
    this.kanbanRepository = kanbanRepository;
    this.projectRepository = projectRepository;
  }

  public List<KanbanProjectSummaryResponse> listProjects() {
    return kanbanRepository.findProjectSummaries().stream()
        .map(row -> new KanbanProjectSummaryResponse(
            row.projectId(),
            row.projectKey(),
            row.projectName(),
            Map.of(
                "OPEN", row.openCount(),
                "IN_PROGRESS", row.inProgressCount(),
                "BLOCKED", row.blockedCount(),
                "DONE", row.doneCount()
            )
        ))
        .collect(Collectors.toList());
  }

  public KanbanBoardResponse getBoard(Long projectId) {
    WorkspaceProjectRepository.ProjectSummaryRow summary = projectRepository.findSummaryById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

    List<WorkspaceKanbanRepository.KanbanIssueRow> rows = kanbanRepository.findIssuesByProject(projectId);
    Map<String, ColumnPolicy> policyByStatus = loadPolicies(projectId);

    Map<String, Integer> totalByStatus = new LinkedHashMap<>();
    STATUS_ORDER.forEach(status -> totalByStatus.put(status, 0));

    Map<String, LaneBuilder> lanes = new LinkedHashMap<>();
    LocalDate today = LocalDate.now();
    LocalDateTime threshold = LocalDateTime.now().minusDays(7);
    List<Integer> wipAges = new ArrayList<>();
    int throughput = 0;

    for (WorkspaceKanbanRepository.KanbanIssueRow row : rows) {
      totalByStatus.compute(row.status(), (status, count) -> count == null ? 1 : count + 1);

      int ageDays = ageInDays(row.updatedAt(), today);
      KanbanIssueCardResponse card = new KanbanIssueCardResponse(
          row.issueId(),
          row.issueKey(),
          row.title(),
          row.priority(),
          row.assigneeName(),
          ageDays
      );

      String laneId = row.assigneeId() == null ? "unassigned" : "user-" + row.assigneeId();
      String laneLabel = row.assigneeId() == null ? "Unassigned" : row.assigneeName();
      lanes.computeIfAbsent(laneId, key -> new LaneBuilder(laneId, laneLabel))
          .add(row.status(), card);

      if (!"DONE".equals(row.status())) {
        wipAges.add(ageDays);
      }
      if ("DONE".equals(row.status()) && !row.updatedAt().isBefore(threshold)) {
        throughput++;
      }
    }

    List<KanbanColumnSummaryResponse> columns = STATUS_ORDER.stream()
        .map(status -> {
          ColumnPolicy policy = policyByStatus.get(status);
          int total = totalByStatus.getOrDefault(status, 0);
          boolean overLimit = policy.wipLimit() != null && policy.wipLimit() > 0 && total > policy.wipLimit();
          return new KanbanColumnSummaryResponse(
              status,
              titleForStatus(status),
              policy.wipLimit(),
              policy.policyText(),
              total,
              overLimit
          );
        })
        .collect(Collectors.toList());

    List<KanbanSwimlaneResponse> swimlanes = lanes.values().stream()
        .sorted((a, b) -> {
          if (Objects.equals(a.laneId(), "unassigned")) {
            return 1;
          }
          if (Objects.equals(b.laneId(), "unassigned")) {
            return -1;
          }
          return a.label().compareToIgnoreCase(b.label());
        })
        .map(builder -> builder.toResponse(STATUS_ORDER))
        .collect(Collectors.toList());

    KanbanFlowMetricsResponse metrics = new KanbanFlowMetricsResponse(
        throughput,
        average(wipAges),
        wipAges.stream().max(Integer::compareTo).orElse(0)
    );

    return new KanbanBoardResponse(summary.id(), summary.projectKey(), summary.name(), columns, swimlanes, metrics);
  }

  private String titleForStatus(String status) {
    return switch (status) {
      case "OPEN" -> "Open";
      case "IN_PROGRESS" -> "In Progress";
      case "BLOCKED" -> "Blocked";
      case "DONE" -> "Done";
      default -> status;
    };
  }

  private Map<String, ColumnPolicy> loadPolicies(Long projectId) {
    Map<String, ColumnPolicy> policies = new LinkedHashMap<>();
    STATUS_ORDER.forEach(status -> {
      ColumnPolicyDefaults defaults = DEFAULT_POLICIES.get(status);
      policies.put(status, new ColumnPolicy(status, defaults.wipLimit(), defaults.policyText()));
    });
    for (WorkspaceKanbanRepository.ColumnPolicyRow row : kanbanRepository.findColumnPoliciesByProject(projectId)) {
      policies.put(row.status(), new ColumnPolicy(row.status(), row.wipLimit(), row.policyText()));
    }
    return policies;
  }

  private int ageInDays(LocalDateTime updatedAt, LocalDate today) {
    if (updatedAt == null) {
      return 0;
    }
    int days = (int) ChronoUnit.DAYS.between(updatedAt.toLocalDate(), today);
    return Math.max(days, 0);
  }

  private double average(List<Integer> values) {
    if (values.isEmpty()) {
      return 0;
    }
    double sum = 0;
    for (int value : values) {
      sum += value;
    }
    return Math.round((sum / values.size()) * 10.0) / 10.0;
  }

  private record ColumnPolicy(String status, Integer wipLimit, String policyText) {}

  private record ColumnPolicyDefaults(Integer wipLimit, String policyText) {}

  private static final class LaneBuilder {
    private final String laneId;
    private final String label;
    private final Map<String, List<KanbanIssueCardResponse>> columns = new LinkedHashMap<>();

    private LaneBuilder(String laneId, String label) {
      this.laneId = laneId;
      this.label = label;
    }

    private void add(String status, KanbanIssueCardResponse card) {
      columns.computeIfAbsent(status, key -> new ArrayList<>()).add(card);
    }

    private KanbanSwimlaneResponse toResponse(List<String> statusOrder) {
      List<KanbanLaneColumnResponse> laneColumns = statusOrder.stream()
          .map(status -> new KanbanLaneColumnResponse(status, Optional.ofNullable(columns.get(status))
              .orElseGet(List::of)))
          .collect(Collectors.toList());
      return new KanbanSwimlaneResponse(laneId, label, laneColumns);
    }

    private String laneId() {
      return laneId;
    }

    private String label() {
      return label;
    }
  }
}
