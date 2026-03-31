package com.phoenixtask.workspace.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
public record GitRepositoryLinkRequest(
    @NotBlank String repoOwner,
    @NotBlank String repoName,
    String defaultBranch
) {}
