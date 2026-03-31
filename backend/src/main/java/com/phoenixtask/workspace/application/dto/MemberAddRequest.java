package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotNull;

public record MemberAddRequest(
    @NotNull Long userId,
    @NotNull Long roleId
) {}
