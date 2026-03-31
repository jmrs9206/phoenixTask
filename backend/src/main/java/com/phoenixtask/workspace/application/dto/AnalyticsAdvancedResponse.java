package com.phoenixtask.workspace.application.dto;

import java.util.List;

public record AnalyticsAdvancedResponse(
    List<AnalyticsCfdPointResponse> cfdSeries,
    boolean cfdApproximate,
    int cfdWindowDays,
    List<AnalyticsCycleTimePointResponse> cycleTimeSeries,
    AnalyticsFlowSummaryResponse flowSummary,
    double mttrDays,
    AnalyticsTechDebtResponse techDebt
) {}
