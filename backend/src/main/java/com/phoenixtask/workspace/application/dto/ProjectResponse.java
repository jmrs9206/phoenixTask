package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record ProjectResponse(
    Long id,
    Long companyId,
    String projectKey,
    String name,
    String description,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
