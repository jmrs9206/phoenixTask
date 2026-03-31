package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamCreateRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 255) String description
) {}
