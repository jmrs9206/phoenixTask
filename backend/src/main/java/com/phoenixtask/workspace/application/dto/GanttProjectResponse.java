package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;

public record GanttProjectResponse(
    Long projectId,
    String projectKey,
    String projectName,
    LocalDate plannedStartDate,
    LocalDate plannedEndDate
) {}
