package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.GanttBaselineResponse;
import com.phoenixtask.workspace.application.dto.GanttCriticalPathItemResponse;
import com.phoenixtask.workspace.application.dto.GanttDependencyResponse;
import com.phoenixtask.workspace.application.dto.GanttIssueResponse;
import com.phoenixtask.workspace.application.dto.GanttProjectDetailResponse;
import com.phoenixtask.workspace.application.dto.GanttProjectResponse;
import com.phoenixtask.workspace.application.dto.GanttResourceLoadResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceGanttRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceGanttService {

  private final WorkspaceGanttRepository ganttRepository;

  public WorkspaceGanttService(WorkspaceGanttRepository ganttRepository) {
    this.ganttRepository = ganttRepository;
  }

  public List<GanttProjectResponse> listProjects() {
    return ganttRepository.findProjects().stream()
        .map(row -> new GanttProjectResponse(
            row.projectId(),
            row.projectKey(),
            row.projectName(),
            row.plannedStartDate(),
            row.plannedEndDate()
        ))
        .collect(Collectors.toList());
  }

  public GanttProjectDetailResponse getProject(Long projectId) {
    WorkspaceGanttRepository.ProjectTimelineRow project = ganttRepository.findProject(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    List<WorkspaceGanttRepository.GanttIssueRow> issueRows = ganttRepository.findIssuesByProject(projectId);
    List<GanttIssueResponse> issues = issueRows.stream()
        .map(row -> new GanttIssueResponse(
            row.issueId(),
            row.issueKey(),
            row.title(),
            row.plannedStartDate(),
            row.dueDate(),
            row.status(),
            row.baselineStartDate(),
            row.baselineEndDate(),
            row.assigneeUserId(),
            row.assigneeName()
        ))
        .collect(Collectors.toList());
    GanttBaselineResponse baseline = ganttRepository.findProjectBaseline(projectId)
        .map(row -> new GanttBaselineResponse(
            row.baselineStartDate(),
            row.baselineEndDate(),
            row.capturedAt(),
            row.capturedByUserId(),
            row.capturedByName()
        ))
        .orElse(null);
    List<GanttDependencyResponse> dependencies = ganttRepository.findDependenciesByProject(projectId).stream()
        .map(row -> new GanttDependencyResponse(
            row.dependencyId(),
            row.predecessorIssueId(),
            row.predecessorIssueKey(),
            row.successorIssueId(),
            row.successorIssueKey(),
            row.dependencyType()
        ))
        .collect(Collectors.toList());
    List<GanttCriticalPathItemResponse> criticalPath = computeCriticalPath(issues, dependencies);
    List<GanttResourceLoadResponse> resourceLoad = computeResourceLoad(issues);
    return new GanttProjectDetailResponse(
        project.projectId(),
        project.projectKey(),
        project.projectName(),
        project.plannedStartDate(),
        project.plannedEndDate(),
        baseline,
        issues,
        dependencies,
        criticalPath,
        resourceLoad
    );
  }

  public GanttBaselineResponse captureBaseline(Long projectId, Long actorId) {
    WorkspaceGanttRepository.ProjectTimelineRow project = ganttRepository.findProject(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    ganttRepository.upsertProjectBaseline(projectId, project.plannedStartDate(), project.plannedEndDate(), actorId);
    List<WorkspaceGanttRepository.GanttIssueRow> issues = ganttRepository.findIssuesByProject(projectId);
    ganttRepository.upsertIssueBaselines(issues, actorId);
    return ganttRepository.findProjectBaseline(projectId)
        .map(row -> new GanttBaselineResponse(
            row.baselineStartDate(),
            row.baselineEndDate(),
            row.capturedAt(),
            row.capturedByUserId(),
            row.capturedByName()
        ))
        .orElse(null);
  }

  public GanttDependencyResponse createDependency(Long projectId, Long predecessorIssueId, Long successorIssueId,
      String dependencyType, Long actorId) {
    if (predecessorIssueId == null || successorIssueId == null) {
      throw new ValidationException("Both predecessorIssueId and successorIssueId are required");
    }
    if (predecessorIssueId.equals(successorIssueId)) {
      throw new ValidationException("Dependency cannot reference the same issue");
    }
    String normalizedType = dependencyType == null || dependencyType.isBlank() ? "FS" : dependencyType.trim().toUpperCase();
    if (!"FS".equals(normalizedType)) {
      throw new ValidationException("Only finish-to-start dependencies are supported");
    }
    Long predecessorProject = ganttRepository.findIssueProjectId(predecessorIssueId)
        .orElseThrow(() -> new ValidationException("Predecessor issue not found"));
    Long successorProject = ganttRepository.findIssueProjectId(successorIssueId)
        .orElseThrow(() -> new ValidationException("Successor issue not found"));
    if (!predecessorProject.equals(projectId) || !successorProject.equals(projectId)) {
      throw new ValidationException("Issues must belong to the same project");
    }
    try {
      WorkspaceGanttRepository.DependencyRow row = ganttRepository.insertDependency(
          predecessorIssueId, successorIssueId, normalizedType, actorId);
      return new GanttDependencyResponse(
          row.dependencyId(),
          row.predecessorIssueId(),
          row.predecessorIssueKey(),
          row.successorIssueId(),
          row.successorIssueKey(),
          row.dependencyType()
      );
    } catch (DuplicateKeyException ex) {
      throw new ValidationException("Dependency already exists");
    }
  }

  private List<GanttResourceLoadResponse> computeResourceLoad(List<GanttIssueResponse> issues) {
    Map<Long, ResourceLoadAccumulator> buckets = new HashMap<>();
    for (GanttIssueResponse issue : issues) {
      if ("DONE".equalsIgnoreCase(issue.status())) {
        continue;
      }
      Long assigneeId = issue.assigneeUserId();
      String assigneeName = issue.assigneeName() != null ? issue.assigneeName() : "Unassigned";
      ResourceLoadAccumulator accumulator = buckets.computeIfAbsent(
          assigneeId != null ? assigneeId : -1L,
          id -> new ResourceLoadAccumulator(assigneeId, assigneeName));
      accumulator.issueCount++;
      accumulator.totalPlannedDays += calculateDurationDays(issue.plannedStartDate(), issue.dueDate());
      accumulator.windowStart = minDate(accumulator.windowStart, issue.plannedStartDate());
      accumulator.windowEnd = maxDate(accumulator.windowEnd, issue.dueDate());
    }
    return buckets.values().stream()
        .sorted(Comparator.comparing(ResourceLoadAccumulator::issueCount).reversed()
            .thenComparing(ResourceLoadAccumulator::assigneeName))
        .map(acc -> new GanttResourceLoadResponse(
            acc.assigneeUserId,
            acc.assigneeName,
            acc.issueCount,
            acc.totalPlannedDays,
            acc.windowStart,
            acc.windowEnd
        ))
        .collect(Collectors.toList());
  }

  private List<GanttCriticalPathItemResponse> computeCriticalPath(List<GanttIssueResponse> issues,
      List<GanttDependencyResponse> dependencies) {
    if (dependencies.isEmpty() || issues.isEmpty()) {
      return List.of();
    }
    Map<Long, GanttIssueResponse> issueMap = issues.stream()
        .collect(Collectors.toMap(GanttIssueResponse::issueId, issue -> issue));
    Map<Long, List<Long>> adjacency = new HashMap<>();
    Map<Long, Integer> indegree = new HashMap<>();
    Map<Long, Integer> durationById = new HashMap<>();
    for (GanttIssueResponse issue : issues) {
      durationById.put(issue.issueId(), calculateDurationDays(issue.plannedStartDate(), issue.dueDate()));
    }
    for (GanttDependencyResponse dependency : dependencies) {
      if (!issueMap.containsKey(dependency.predecessorIssueId())
          || !issueMap.containsKey(dependency.successorIssueId())) {
        continue;
      }
      adjacency.computeIfAbsent(dependency.predecessorIssueId(), key -> new ArrayList<>())
          .add(dependency.successorIssueId());
      indegree.put(dependency.successorIssueId(), indegree.getOrDefault(dependency.successorIssueId(), 0) + 1);
      indegree.putIfAbsent(dependency.predecessorIssueId(), indegree.getOrDefault(dependency.predecessorIssueId(), 0));
    }
    Deque<Long> queue = new ArrayDeque<>();
    for (Long issueId : issueMap.keySet()) {
      if (indegree.getOrDefault(issueId, 0) == 0) {
        queue.add(issueId);
      }
    }
    Map<Long, Integer> longest = new HashMap<>();
    Map<Long, Long> parent = new HashMap<>();
    for (Long issueId : issueMap.keySet()) {
      longest.put(issueId, durationById.getOrDefault(issueId, 1));
    }
    while (!queue.isEmpty()) {
      Long current = queue.remove();
      int currentLength = longest.getOrDefault(current, 1);
      for (Long successor : adjacency.getOrDefault(current, List.of())) {
        int successorDuration = durationById.getOrDefault(successor, 1);
        int candidate = currentLength + successorDuration;
        if (candidate > longest.getOrDefault(successor, 1)) {
          longest.put(successor, candidate);
          parent.put(successor, current);
        }
        int nextIndegree = indegree.getOrDefault(successor, 0) - 1;
        indegree.put(successor, nextIndegree);
        if (nextIndegree == 0) {
          queue.add(successor);
        }
      }
    }
    Optional<Map.Entry<Long, Integer>> maxEntry = longest.entrySet().stream()
        .max(Map.Entry.comparingByValue());
    if (maxEntry.isEmpty()) {
      return List.of();
    }
    List<Long> chain = new ArrayList<>();
    Long cursor = maxEntry.get().getKey();
    chain.add(cursor);
    while (parent.containsKey(cursor)) {
      cursor = parent.get(cursor);
      chain.add(cursor);
    }
    List<GanttCriticalPathItemResponse> path = new ArrayList<>();
    for (int i = chain.size() - 1; i >= 0; i--) {
      GanttIssueResponse issue = issueMap.get(chain.get(i));
      if (issue == null) {
        continue;
      }
      path.add(new GanttCriticalPathItemResponse(
          issue.issueId(),
          issue.issueKey(),
          issue.title(),
          issue.plannedStartDate(),
          issue.dueDate(),
          calculateDurationDays(issue.plannedStartDate(), issue.dueDate())
      ));
    }
    return path;
  }

  private int calculateDurationDays(LocalDate start, LocalDate end) {
    if (start != null && end != null && !end.isBefore(start)) {
      return Math.max(1, (int) ChronoUnit.DAYS.between(start, end) + 1);
    }
    return 1;
  }

  private LocalDate minDate(LocalDate current, LocalDate candidate) {
    if (candidate == null) {
      return current;
    }
    if (current == null || candidate.isBefore(current)) {
      return candidate;
    }
    return current;
  }

  private LocalDate maxDate(LocalDate current, LocalDate candidate) {
    if (candidate == null) {
      return current;
    }
    if (current == null || candidate.isAfter(current)) {
      return candidate;
    }
    return current;
  }

  private static final class ResourceLoadAccumulator {
    private final Long assigneeUserId;
    private final String assigneeName;
    private int issueCount;
    private int totalPlannedDays;
    private LocalDate windowStart;
    private LocalDate windowEnd;

    private ResourceLoadAccumulator(Long assigneeUserId, String assigneeName) {
      this.assigneeUserId = assigneeUserId;
      this.assigneeName = assigneeName;
    }

    private int issueCount() {
      return issueCount;
    }

    private String assigneeName() {
      return assigneeName;
    }
  }
}
