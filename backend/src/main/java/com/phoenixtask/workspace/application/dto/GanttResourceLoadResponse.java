package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;

public record GanttResourceLoadResponse(
    Long assigneeUserId,
    String assigneeName,
    int issueCount,
    int totalPlannedDays,
    LocalDate windowStart,
    LocalDate windowEnd
) {}
