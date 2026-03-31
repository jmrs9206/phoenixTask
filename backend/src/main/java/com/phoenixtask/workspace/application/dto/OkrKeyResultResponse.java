package com.phoenixtask.workspace.application.dto;

import java.math.BigDecimal;

public record OkrKeyResultResponse(
    Long id,
    Long objectiveId,
    Long projectId,
    String projectKey,
    String projectName,
    String title,
    BigDecimal targetValue,
    BigDecimal currentValue,
    String unit,
    String status
) {}
