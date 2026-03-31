package com.phoenixtask.workspace.application.dto;

public record IssueSummaryResponse(
    Long id,
    String issueKey,
    String title,
    String status,
    String priority,
    String assigneeName
) {}
