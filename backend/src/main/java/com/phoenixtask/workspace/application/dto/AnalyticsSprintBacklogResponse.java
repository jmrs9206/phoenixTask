package com.phoenixtask.workspace.application.dto;

public record AnalyticsSprintBacklogResponse(
    Long projectId,
    String projectKey,
    Long backlog,
    Long activeSprint
) {}
