package com.phoenixtask.workspace.interfaces.dto;

import java.time.LocalDateTime;

public record GitIntegrationResponse(
    Long id,
    String provider,
    String label,
    String status,
    String tokenPrefix,
    LocalDateTime createdAt,
    LocalDateTime revokedAt
) {}
