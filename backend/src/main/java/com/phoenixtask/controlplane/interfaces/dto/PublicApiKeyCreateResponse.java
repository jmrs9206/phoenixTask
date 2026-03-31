package com.phoenixtask.controlplane.interfaces.dto;

import java.time.OffsetDateTime;
import java.util.Set;

public record PublicApiKeyCreateResponse(
    Long id,
    String tenantCode,
    String label,
    String keyPrefix,
    String key,
    Set<String> scopes,
    String status,
    OffsetDateTime createdAt
) {}
