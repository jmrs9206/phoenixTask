package com.phoenixtask.publicapi.domain;

import java.time.OffsetDateTime;
import java.util.Set;

public record PublicApiKeyRecord(
    Long id,
    String tenantCode,
    String label,
    String keyPrefix,
    Set<String> scopes,
    PublicApiKeyStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime revokedAt
) {}
