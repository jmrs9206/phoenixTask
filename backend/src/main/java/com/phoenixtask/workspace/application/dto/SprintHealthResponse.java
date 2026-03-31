package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;
import java.util.List;

public record SprintHealthResponse(
    Long sprintId,
    Long projectId,
    String name,
    String goal,
    String status,
    LocalDate startDate,
    LocalDate endDate,
    String healthStatus,
    Double sayDoPercent,
    SprintCommitmentResponse commitment,
    List<SprintBurndownPointResponse> burndown
) {}
