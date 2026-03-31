package com.phoenixtask.workspace.application.dto;

import java.util.List;

public record WorkspacePermissionsResponse(
    Long roleId,
    List<String> permissions
) {}
