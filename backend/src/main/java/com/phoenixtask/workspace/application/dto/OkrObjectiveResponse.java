package com.phoenixtask.workspace.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OkrObjectiveResponse(
    Long id,
    Long projectId,
    String projectKey,
    String projectName,
    String title,
    String description,
    String status,
    LocalDate periodStart,
    LocalDate periodEnd,
    Long ownerUserId,
    String ownerName,
    double progress,
    String confidenceLevel,
    BigDecimal finalScore,
    LocalDateTime closedAt
) {}
