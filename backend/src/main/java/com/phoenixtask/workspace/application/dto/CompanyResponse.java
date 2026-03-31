package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record CompanyResponse(
    Long id,
    String code,
    String name,
    String status,
    Long ownerUserId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    CompanySettingsResponse settings
) {}
