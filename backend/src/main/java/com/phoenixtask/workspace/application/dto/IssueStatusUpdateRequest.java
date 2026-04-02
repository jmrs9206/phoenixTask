package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotBlank;

public record IssueStatusUpdateRequest(
    @NotBlank String status
) {}
