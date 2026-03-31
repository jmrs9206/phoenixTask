package com.phoenixtask.controlplane.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record PublicApiKeyCreateRequest(
    @NotBlank String label,
    @NotEmpty Set<String> scopes
) {}
