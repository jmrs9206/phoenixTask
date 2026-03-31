package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record MessageThreadTeamResponse(
    Long threadId,
    String threadType,
    Long teamId,
    String teamName,
    LocalDateTime lastMessageAt
) {}
