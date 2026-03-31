package com.phoenixtask.workspace.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record GitIntegrationCreateRequest(
    @NotBlank String provider,
    @NotBlank String label,
    @NotBlank String token,
    Boolean validate
) {}
