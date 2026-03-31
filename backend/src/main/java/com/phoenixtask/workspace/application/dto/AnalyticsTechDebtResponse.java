package com.phoenixtask.workspace.application.dto;

public record AnalyticsTechDebtResponse(
    long debtCount,
    long totalOpenCount,
    double ratio
) {}
