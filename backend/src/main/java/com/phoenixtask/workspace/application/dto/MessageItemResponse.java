package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record MessageItemResponse(
    Long messageId,
    Long authorUserId,
    String authorFullName,
    String body,
    LocalDateTime createdAt
) {}
