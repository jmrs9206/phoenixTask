package com.phoenixtask.workspace.application.dto;

import java.util.List;

public record KanbanSwimlaneResponse(
    String laneId,
    String label,
    List<KanbanLaneColumnResponse> columns
) {}
