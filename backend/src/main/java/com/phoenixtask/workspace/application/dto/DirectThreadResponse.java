package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record DirectThreadResponse(
    Long threadId,
    String threadType,
    Long directUserOneId,
    Long directUserTwoId,
    LocalDateTime lastMessageAt
) {}
