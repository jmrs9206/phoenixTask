package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;

public record SprintBurndownPointResponse(
    LocalDate date,
    int committedCount,
    int remainingCount
) {}
