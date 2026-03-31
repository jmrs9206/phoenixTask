package com.phoenixtask.publicapi.interfaces.dto;

import java.time.LocalDate;

public record PublicApiIssueDetailResponse(
    Long id,
    String issueKey,
    String title,
    String projectKey,
    String status,
    String priority,
    LocalDate dueDate,
    String description
) {}
