package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record CompanySettingsResponse(
    Long id,
    String timezone,
    String locale,
    String weekStart,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
