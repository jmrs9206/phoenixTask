package com.phoenixtask.controlplane.application;

import java.time.OffsetDateTime;

public record ControlplaneAdminKeyRecord(
    Long id,
    String label,
    String keyPrefix,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime revokedAt
) {}
