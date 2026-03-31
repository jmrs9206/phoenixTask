package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;

public record GanttCriticalPathItemResponse(
    Long issueId,
    String issueKey,
    String title,
    LocalDate plannedStartDate,
    LocalDate dueDate,
    int durationDays
) {}
