package com.phoenixtask.workspace.application.dto;

import java.util.List;

public record ScrumOverviewResponse(
    ScrumSnapshotResponse snapshot,
    List<ScrumActiveSprintResponse> activeSprints,
    List<ScrumIssueCardResponse> activeSprintIssues,
    List<ScrumIssueCardResponse> backlog
) {}
