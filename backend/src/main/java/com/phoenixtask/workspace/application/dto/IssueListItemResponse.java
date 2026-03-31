package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record IssueListItemResponse(
    Long id,
    String issueKey,
    String title,
    String projectKey,
    String status,
    String priority,
    String assigneeName,
    LocalDateTime createdAt
) {}
