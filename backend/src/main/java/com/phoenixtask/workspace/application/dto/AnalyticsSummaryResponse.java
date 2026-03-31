package com.phoenixtask.workspace.application.dto;

import java.util.List;
import java.util.Map;

public record AnalyticsSummaryResponse(
    Map<String, Integer> issuesByStatus,
    Map<String, Integer> issuesByPriority,
    List<AnalyticsIssuesByProjectResponse> issuesByProject,
    List<AnalyticsSprintBacklogResponse> sprintBacklogCounts,
    Long teamCount,
    Long projectCount,
    Long userCount,
    Long messageCount
) {}
