package com.phoenixtask.workspace.application.dto;

import java.util.List;

public record KanbanBoardResponse(
    Long projectId,
    String projectKey,
    String projectName,
    List<KanbanColumnSummaryResponse> columns,
    List<KanbanSwimlaneResponse> swimlanes,
    KanbanFlowMetricsResponse metrics
) {}
