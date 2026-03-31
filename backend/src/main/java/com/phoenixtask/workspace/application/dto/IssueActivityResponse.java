package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record IssueActivityResponse(
    Long id,
    Long issueId,
    Long actorId,
    String actorName,
    String eventType,
    Map<String, Object> metadata,
    LocalDateTime createdAt
) {}
