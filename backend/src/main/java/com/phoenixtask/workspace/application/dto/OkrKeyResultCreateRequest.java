package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OkrKeyResultCreateRequest(
    @NotBlank @Size(max = 200) String title,
    @NotNull BigDecimal targetValue,
    @NotNull BigDecimal currentValue,
    @NotBlank @Size(max = 32) String unit,
    @NotBlank @Size(max = 16) String status
) {}
