package com.phoenixtask.workspace.application.dto;

public record ScrumIssueCardResponse(
    Long id,
    String issueKey,
    String title,
    String status,
    String priority,
    String category,
    String assigneeName,
    String projectKey,
    String projectName,
    String sprintName
) {}
