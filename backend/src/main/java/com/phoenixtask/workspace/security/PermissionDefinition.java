package com.phoenixtask.workspace.security;

public record PermissionDefinition(
    PermissionModule module,
    String action,
    PermissionCode code,
    String description
) {}
