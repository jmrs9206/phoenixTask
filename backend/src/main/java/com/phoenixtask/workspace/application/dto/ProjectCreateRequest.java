package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
    @NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9]{1,14}$") String projectKey,
    @NotBlank @Size(max = 200) String name,
    @Size(max = 255) String description
) {}
