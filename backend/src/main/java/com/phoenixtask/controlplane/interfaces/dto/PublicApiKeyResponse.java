package com.phoenixtask.controlplane.interfaces.dto;

import java.time.OffsetDateTime;
import java.util.Set;

public record PublicApiKeyResponse(
    Long id,
    String tenantCode,
    String label,
    String keyPrefix,
    Set<String> scopes,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime revokedAt
) {}
