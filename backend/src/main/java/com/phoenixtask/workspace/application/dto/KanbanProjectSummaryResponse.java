package com.phoenixtask.workspace.application.dto;

import java.util.Map;

public record KanbanProjectSummaryResponse(
    Long projectId,
    String projectKey,
    String projectName,
    Map<String, Integer> issueCounts
) {}
