package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record OkrObjectiveCreateRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 500) String description,
    @NotNull Long ownerUserId,
    @NotBlank @Size(max = 16) String status,
    @NotNull LocalDate periodStart,
    @NotNull LocalDate periodEnd
) {}
