package com.phoenixtask.workspace.application.dto;

public record KanbanFlowMetricsResponse(
    int throughputLast7Days,
    double averageWipAgeDays,
    int oldestWipAgeDays
) {}
