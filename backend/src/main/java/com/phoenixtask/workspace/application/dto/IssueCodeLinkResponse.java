package com.phoenixtask.workspace.application.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record IssueCodeLinkResponse(
    Long id,
    Long issueId,
    String provider,
    String artifactType,
    String externalId,
    String title,
    String url,
    String authorName,
    Instant externalCreatedAt,
    LocalDateTime createdAt,
    String repoOwner,
    String repoName
) {}
