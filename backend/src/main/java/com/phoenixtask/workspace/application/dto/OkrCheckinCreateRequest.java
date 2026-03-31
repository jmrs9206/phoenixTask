package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OkrCheckinCreateRequest(
    @Min(0) @Max(100) Double progressPercent,
    @NotBlank String confidenceLevel,
    @Size(max = 500) String note
) {}
