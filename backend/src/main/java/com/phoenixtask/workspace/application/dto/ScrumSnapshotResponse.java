package com.phoenixtask.workspace.application.dto;

public record ScrumSnapshotResponse(
    int backlogCount,
    int activeSprintCount,
    int activeSprintIssueCount
) {}
