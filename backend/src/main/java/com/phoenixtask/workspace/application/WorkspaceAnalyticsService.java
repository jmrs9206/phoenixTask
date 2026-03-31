package com.phoenixtask.workspace.application;

import com.phoenixtask.workspace.application.dto.AnalyticsIssuesByProjectResponse;
import com.phoenixtask.workspace.application.dto.AnalyticsAdvancedResponse;
import com.phoenixtask.workspace.application.dto.AnalyticsCfdPointResponse;
import com.phoenixtask.workspace.application.dto.AnalyticsCycleTimePointResponse;
import com.phoenixtask.workspace.application.dto.AnalyticsFlowSummaryResponse;
import com.phoenixtask.workspace.application.dto.AnalyticsSprintBacklogResponse;
import com.phoenixtask.workspace.application.dto.AnalyticsSummaryResponse;
import com.phoenixtask.workspace.application.dto.AnalyticsTechDebtResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceAnalyticsRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceAnalyticsService {

  private static final Set<String> STATUS_KEYS = Set.of("OPEN", "IN_PROGRESS", "BLOCKED", "DONE");
  private static final Set<String> PRIORITY_KEYS = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
  private static final int CFD_WINDOW_DAYS = 14;
  private static final int CYCLE_TIME_LIMIT = 20;
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

  private final WorkspaceAnalyticsRepository analyticsRepository;

  public WorkspaceAnalyticsService(WorkspaceAnalyticsRepository analyticsRepository) {
    this.analyticsRepository = analyticsRepository;
  }

  public AnalyticsSummaryResponse getSummary() {
    Map<String, Integer> issuesByStatus = buildMap(STATUS_KEYS, analyticsRepository.countIssuesByStatus());
    Map<String, Integer> issuesByPriority = buildMap(PRIORITY_KEYS, analyticsRepository.countIssuesByPriority());

    List<AnalyticsIssuesByProjectResponse> issuesByProject = analyticsRepository.countIssuesByProject().stream()
        .map(row -> new AnalyticsIssuesByProjectResponse(row.projectId(), row.projectKey(), row.count()))
        .collect(Collectors.toList());

    List<AnalyticsSprintBacklogResponse> sprintBacklogCounts = analyticsRepository.sprintBacklogCounts().stream()
        .map(row -> new AnalyticsSprintBacklogResponse(
            row.projectId(),
            row.projectKey(),
            row.backlog(),
            row.activeSprint()
        ))
        .collect(Collectors.toList());

    return new AnalyticsSummaryResponse(
        issuesByStatus,
        issuesByPriority,
        issuesByProject,
        sprintBacklogCounts,
        analyticsRepository.countTeams(),
        analyticsRepository.countProjects(),
        analyticsRepository.countUsers(),
        analyticsRepository.countMessages()
    );
  }

  public AnalyticsAdvancedResponse getAdvanced() {
    List<AnalyticsCfdPointResponse> cfdSeries = analyticsRepository.cfdSeries(CFD_WINDOW_DAYS).stream()
        .map(row -> {
          int total = row.open() + row.inProgress() + row.blocked() + row.done();
          return new AnalyticsCfdPointResponse(
              row.day().format(DATE_FORMAT),
              row.open(),
              row.inProgress(),
              row.blocked(),
              row.done(),
              total
          );
        })
        .collect(Collectors.toList());

    List<WorkspaceAnalyticsRepository.CycleTimeRow> cycleTimeRows = analyticsRepository.completedCycleTimes(CYCLE_TIME_LIMIT);

    List<AnalyticsCycleTimePointResponse> cycleTimeSeries = cycleTimeRows.stream()
        .map(row -> new AnalyticsCycleTimePointResponse(
            row.issueKey(),
            row.title(),
            row.completedOn().format(DATE_FORMAT),
            row.cycleTimeDays().setScale(1, RoundingMode.HALF_UP).doubleValue()
        ))
        .collect(Collectors.toList());

    double[] cycleTimes = cycleTimeRows.stream()
        .map(WorkspaceAnalyticsRepository.CycleTimeRow::cycleTimeDays)
        .map(BigDecimal::doubleValue)
        .sorted()
        .mapToDouble(Double::doubleValue)
        .toArray();

    AnalyticsFlowSummaryResponse flowSummary = new AnalyticsFlowSummaryResponse(
        percentile(cycleTimes, 50),
        percentile(cycleTimes, 85),
        cycleTimes.length
    );

    double mttrDays = cycleTimes.length == 0 ? 0 : round1(DoubleStream.of(cycleTimes).average().orElse(0));

    WorkspaceAnalyticsRepository.TechDebtRow techDebtRow = analyticsRepository.techDebtCounts();
    double techDebtRatio = techDebtRow.totalOpen() == 0 ? 0 : round1((techDebtRow.debtCount() * 100.0) / techDebtRow.totalOpen());

    AnalyticsTechDebtResponse techDebt = new AnalyticsTechDebtResponse(
        techDebtRow.debtCount(),
        techDebtRow.totalOpen(),
        techDebtRatio
    );

    return new AnalyticsAdvancedResponse(
        cfdSeries,
        true,
        CFD_WINDOW_DAYS,
        cycleTimeSeries,
        flowSummary,
        mttrDays,
        techDebt
    );
  }

  private Map<String, Integer> buildMap(Set<String> keys, List<WorkspaceAnalyticsRepository.CountByStatusRow> rows) {
    Map<String, Integer> result = new HashMap<>();
    for (String key : keys) {
      result.put(key, 0);
    }
    for (WorkspaceAnalyticsRepository.CountByStatusRow row : rows) {
      if (keys.contains(row.status())) {
        result.put(row.status(), Math.toIntExact(row.count()));
      }
    }
    return result;
  }

  private double percentile(double[] values, int percentile) {
    if (values.length == 0) {
      return 0;
    }
    int index = (int) Math.ceil((percentile / 100.0) * values.length) - 1;
    int safeIndex = Math.min(Math.max(index, 0), values.length - 1);
    return round1(values[safeIndex]);
  }

  private double round1(double value) {
    return Math.round(value * 10.0) / 10.0;
  }
}
