package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.Size;

public record SprintUpdateRequest(
    @Size(max = 255) String goal,
    String status
) {}
