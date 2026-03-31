package com.phoenixtask.workspace.application.dto;

public record AnalyticsFlowSummaryResponse(
    double medianDays,
    double p85Days,
    int sampleSize
) {}
