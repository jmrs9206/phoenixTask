package com.phoenixtask.workspace.application.dto;

import java.util.List;

public record KanbanLaneColumnResponse(
    String status,
    List<KanbanIssueCardResponse> issues
) {}
