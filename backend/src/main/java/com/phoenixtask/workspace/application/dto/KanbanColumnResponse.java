package com.phoenixtask.workspace.application.dto;

import java.util.List;

public record KanbanColumnResponse(
    String status,
    String title,
    List<KanbanIssueCardResponse> issues
) {}
