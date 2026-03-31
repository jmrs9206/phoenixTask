package com.phoenixtask.workspace.interfaces.dto;

import java.time.LocalDateTime;

public record GitRepositoryResponse(
    Long id,
    Long integrationId,
    Long projectId,
    String provider,
    String repoOwner,
    String repoName,
    String fullName,
    String defaultBranch,
    String status,
    LocalDateTime createdAt
) {}
