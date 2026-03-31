package com.phoenixtask.workspace.application.dto;

public record OkrInitiativeResponse(
    Long id,
    Long issueId,
    String issueKey,
    String issueTitle,
    Long projectId,
    String projectKey,
    String projectName
) {}
