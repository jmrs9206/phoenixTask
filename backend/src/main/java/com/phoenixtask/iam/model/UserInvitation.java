package com.phoenixtask.iam.model;

import java.time.LocalDateTime;

public record UserInvitation(
    Long id,
    String email,
    String tokenHash,
    LocalDateTime expiresAt,
    LocalDateTime usedAt,
    Long invitedByUserId,
    LocalDateTime createdAt
) {}
