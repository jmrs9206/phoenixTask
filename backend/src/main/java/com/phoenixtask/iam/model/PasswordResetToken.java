package com.phoenixtask.iam.model;

import java.time.LocalDateTime;

public record PasswordResetToken(
    Long id,
    Long userId,
    String tokenHash,
    LocalDateTime expiresAt,
    LocalDateTime usedAt,
    LocalDateTime createdAt
) {}
