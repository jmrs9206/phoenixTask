package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;
import java.util.List;

public record GanttProjectDetailResponse(
    Long projectId,
    String projectKey,
    String projectName,
    LocalDate plannedStartDate,
    LocalDate plannedEndDate,
    GanttBaselineResponse baseline,
    List<GanttIssueResponse> issues,
    List<GanttDependencyResponse> dependencies,
    List<GanttCriticalPathItemResponse> criticalPath,
    List<GanttResourceLoadResponse> resourceLoad
) {}
