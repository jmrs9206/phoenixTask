package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record SprintCommitmentResponse(
    LocalDateTime capturedAt,
    int committedCount,
    int completedCount,
    int remainingCount
) {}
