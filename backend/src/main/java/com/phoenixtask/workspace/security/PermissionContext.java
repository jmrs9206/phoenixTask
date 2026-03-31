package com.phoenixtask.workspace.security;

public record PermissionContext(
    PermissionScope scope,
    Long companyId,
    Long teamId,
    Long projectId,
    Long issueId,
    Long threadId
) {

  public static PermissionContext company(Long companyId) {
    return new PermissionContext(PermissionScope.COMPANY, companyId, null, null, null, null);
  }

  public static PermissionContext team(Long teamId) {
    return new PermissionContext(PermissionScope.TEAM, null, teamId, null, null, null);
  }

  public static PermissionContext project(Long projectId) {
    return new PermissionContext(PermissionScope.PROJECT, null, null, projectId, null, null);
  }

  public static PermissionContext issue(Long issueId) {
    return new PermissionContext(PermissionScope.ISSUE, null, null, null, issueId, null);
  }

  public static PermissionContext thread(Long threadId) {
    return new PermissionContext(PermissionScope.THREAD, null, null, null, null, threadId);
  }
}
