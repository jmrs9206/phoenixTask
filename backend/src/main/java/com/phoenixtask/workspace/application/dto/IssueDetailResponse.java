package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record IssueDetailResponse(
    Long id,
    String issueKey,
    String title,
    String description,
    String projectKey,
    String status,
    String priority,
    String reporterName,
    String assigneeName,
    LocalDate dueDate,
    LocalDateTime createdAt
) {}
