package com.phoenixtask.workspace.application;

import java.time.Instant;

public record IssueCodeLinkCandidate(
    String issueKey,
    IssueCodeArtifactType artifactType,
    String externalId,
    String title,
    String url,
    String authorName,
    Instant externalCreatedAt
) {}
