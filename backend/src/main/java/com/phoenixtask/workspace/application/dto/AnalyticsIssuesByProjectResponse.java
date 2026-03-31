package com.phoenixtask.workspace.application.dto;

public record AnalyticsIssuesByProjectResponse(
    Long projectId,
    String projectKey,
    Long count
) {}
