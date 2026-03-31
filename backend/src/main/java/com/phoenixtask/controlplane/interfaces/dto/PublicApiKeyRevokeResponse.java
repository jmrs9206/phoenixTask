package com.phoenixtask.controlplane.interfaces.dto;

import java.time.OffsetDateTime;

public record PublicApiKeyRevokeResponse(
    Long id,
    String status,
    OffsetDateTime revokedAt
) {}
