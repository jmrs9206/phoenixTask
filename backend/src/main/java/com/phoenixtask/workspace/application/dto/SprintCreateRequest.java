package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SprintCreateRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 255) String goal,
    @NotBlank @Size(max = 16) String status,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {}
