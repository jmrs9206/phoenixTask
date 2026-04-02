package com.phoenixtask.workspace.application.dto;

public record ScrumProjectSummaryResponse(
    Long projectId,
    String projectKey,
    String projectName
) {}
