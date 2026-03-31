package com.phoenixtask.workspace.application.dto;

public record KanbanIssueCardResponse(
    Long issueId,
    String issueKey,
    String title,
    String priority,
    String assigneeName,
    int ageDays
) {}
