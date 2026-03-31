package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record MessageThreadProjectResponse(
    Long threadId,
    String threadType,
    Long projectId,
    String projectKey,
    String projectName,
    LocalDateTime lastMessageAt
) {}
