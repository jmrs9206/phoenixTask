package com.phoenixtask.workspace.interfaces.dto;

public record GanttDependencyCreateRequest(
    Long predecessorIssueId,
    Long successorIssueId,
    String dependencyType
) {}
