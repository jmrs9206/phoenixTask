package com.phoenixtask.workspace.application.dto;

public record AnalyticsCycleTimePointResponse(
    String issueKey,
    String title,
    String completedOn,
    double cycleTimeDays
) {}
