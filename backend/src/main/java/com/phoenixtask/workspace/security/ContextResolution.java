package com.phoenixtask.workspace.security;

public record ContextResolution(
    ContextResolutionStatus status,
    PermissionContext context
) {

  public static ContextResolution ok(PermissionContext context) {
    return new ContextResolution(ContextResolutionStatus.OK, context);
  }

  public static ContextResolution notFound() {
    return new ContextResolution(ContextResolutionStatus.NOT_FOUND, null);
  }
}
