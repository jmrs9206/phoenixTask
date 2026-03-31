package com.phoenixtask.workspace.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GanttBaselineResponse(
    LocalDate baselineStartDate,
    LocalDate baselineEndDate,
    LocalDateTime capturedAt,
    Long capturedByUserId,
    String capturedByName
) {}
