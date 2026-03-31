package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;

public record GanttIssueResponse(
    Long issueId,
    String issueKey,
    String title,
    LocalDate plannedStartDate,
    LocalDate dueDate,
    String status,
    LocalDate baselineStartDate,
    LocalDate baselineEndDate,
    Long assigneeUserId,
    String assigneeName
) {}
