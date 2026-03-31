package com.phoenixtask.workspace.application.dto;

public record IssueListQuery(
    Long projectId,
    String status,
    String priority,
    Long assigneeId,
    String query
) {}
