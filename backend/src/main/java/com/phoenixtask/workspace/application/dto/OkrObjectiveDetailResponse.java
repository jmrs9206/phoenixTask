package com.phoenixtask.workspace.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OkrObjectiveDetailResponse(
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
    List<OkrKeyResultResponse> keyResults,
    String confidenceLevel,
    BigDecimal finalScore,
    LocalDateTime closedAt,
    List<OkrCheckinResponse> checkins,
    List<OkrInitiativeResponse> initiatives
) {}
