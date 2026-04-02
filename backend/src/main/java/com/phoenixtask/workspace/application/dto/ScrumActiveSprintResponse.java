package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;

public record ScrumActiveSprintResponse(
    Long sprintId,
    Long projectId,
    String projectKey,
    String projectName,
    String name,
    LocalDate startDate,
    LocalDate endDate
) {}
