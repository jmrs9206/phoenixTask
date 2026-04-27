package com.phoenixtask.iam.model;

import java.time.LocalDateTime;

public record User(
    Long id,
    String email,
    String displayName,
    String passwordHash,
    String status,
    boolean isPlatformInternal,
    boolean mustChangePassword,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime activatedAt
) {}
