package com.phoenixtask.workspace.interfaces.dto;

import java.time.LocalDateTime;

public record GitIntegrationCreateResponse(
    Long id,
    String provider,
    String label,
    String status,
    String tokenPrefix,
    String webhookSecret,
    String webhookPath,
    LocalDateTime createdAt
) {}
