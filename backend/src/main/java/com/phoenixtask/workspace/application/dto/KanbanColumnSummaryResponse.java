package com.phoenixtask.workspace.application.dto;

public record KanbanColumnSummaryResponse(
    String status,
    String title,
    Integer wipLimit,
    String policy,
    int totalCount,
    boolean overLimit
) {}
