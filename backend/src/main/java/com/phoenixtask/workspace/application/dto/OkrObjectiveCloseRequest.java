package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OkrObjectiveCloseRequest(
    @NotBlank String status,
    @NotNull @Min(0) @Max(100) Double finalScore
) {}
