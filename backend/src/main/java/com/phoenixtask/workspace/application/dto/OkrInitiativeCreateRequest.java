package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotNull;

public record OkrInitiativeCreateRequest(
    @NotNull Long issueId
) {}
