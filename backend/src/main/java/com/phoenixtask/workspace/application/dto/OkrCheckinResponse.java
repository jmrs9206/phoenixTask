package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record OkrCheckinResponse(
    Long id,
    Long authorUserId,
    String authorName,
    Double progressPercent,
    String confidenceLevel,
    String note,
    LocalDateTime createdAt
) {}
