package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record IssueCommentResponse(
    Long id,
    Long issueId,
    Long authorId,
    String authorName,
    String body,
    LocalDateTime createdAt
) {}
