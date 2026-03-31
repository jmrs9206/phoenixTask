package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SprintResponse(
    Long id,
    Long projectId,
    String name,
    String goal,
    String status,
    LocalDate startDate,
    LocalDate endDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
