package com.phoenixtask.iam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class IamRequests {
    public record UpdateStatusRequest(
        @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE|INVITED|LOCKED") String status
    ) {}

    public record AddRoleRequest(
        @NotBlank String role
    ) {}
}
