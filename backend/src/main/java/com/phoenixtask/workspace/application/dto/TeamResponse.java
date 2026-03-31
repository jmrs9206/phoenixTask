package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record TeamResponse(
    Long id,
    Long companyId,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
