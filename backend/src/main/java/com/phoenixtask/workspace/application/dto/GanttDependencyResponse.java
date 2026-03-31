package com.phoenixtask.workspace.application.dto;

public record GanttDependencyResponse(
    Long dependencyId,
    Long predecessorIssueId,
    String predecessorIssueKey,
    Long successorIssueId,
    String successorIssueKey,
    String dependencyType
) {}
