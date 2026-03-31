package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record MessageThreadDirectResponse(
    Long threadId,
    String threadType,
    Long otherUserId,
    String otherUserFullName,
    String otherUserEmail,
    LocalDateTime lastMessageAt
) {}
