package com.phoenixtask.workspace.application.dto;

public record AnalyticsCfdPointResponse(
    String date,
    int open,
    int inProgress,
    int blocked,
    int done,
    int total
) {}
