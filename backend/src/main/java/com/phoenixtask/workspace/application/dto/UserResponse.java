package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    Long companyId,
    Long primaryRoleId,
    String firstName,
    String lastName,
    String email,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
