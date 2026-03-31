package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IssueCreateRequest(
    @NotNull Long projectId,
    @NotBlank @Size(max = 200) String title,
    @Size(max = 1000) String description,
    @NotNull Long reporterUserId,
    Long assigneeUserId,
    @NotBlank String status,
    @NotBlank String priority
) {}
