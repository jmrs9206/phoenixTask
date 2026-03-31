package com.phoenixtask.workspace.security;

public enum PermissionCode {
  COMPANY_VIEW("company.view"),
  COMPANY_UPDATE("company.update"),
  COMPANY_SETTINGS_VIEW("company.settings.view"),
  COMPANY_SETTINGS_UPDATE("company.settings.update"),
  USERS_VIEW("users.view"),
  USERS_CREATE("users.create"),
  USERS_UPDATE("users.update"),
  USERS_DEACTIVATE("users.deactivate"),
  TEAMS_VIEW("teams.view"),
  TEAMS_CREATE("teams.create"),
  TEAMS_UPDATE("teams.update"),
  TEAMS_DELETE("teams.delete"),
  TEAM_MEMBERS_VIEW("team.members.view"),
  TEAM_MEMBERS_ADD("team.members.add"),
  TEAM_MEMBERS_REMOVE("team.members.remove"),
  TEAM_MEMBERS_ROLE_UPDATE("team.members.role.update"),
  PROJECTS_VIEW("projects.view"),
  PROJECTS_CREATE("projects.create"),
  PROJECTS_UPDATE("projects.update"),
  PROJECTS_ARCHIVE("projects.archive"),
  PROJECTS_DELETE("projects.delete"),
  PROJECT_MEMBERS_VIEW("project.members.view"),
  PROJECT_MEMBERS_ADD("project.members.add"),
  PROJECT_MEMBERS_REMOVE("project.members.remove"),
  PROJECT_MEMBERS_ROLE_UPDATE("project.members.role.update"),
  ROLES_VIEW("roles.view"),
  ROLES_ASSIGN("roles.assign"),
  PERMISSIONS_VIEW("permissions.view"),
  ISSUES_VIEW("issues.view"),
  ISSUES_CREATE("issues.create"),
  ISSUES_UPDATE("issues.update"),
  ISSUES_ASSIGN("issues.assign"),
  ISSUES_TRANSITION("issues.transition"),
  ISSUES_COMMENT_VIEW("issues.comment.view"),
  ISSUES_COMMENT_CREATE("issues.comment.create"),
  ISSUES_COMMENT_UPDATE("issues.comment.update"),
  ISSUES_COMMENT_DELETE("issues.comment.delete"),
  ISSUES_ATTACHMENT_UPLOAD("issues.attachment.upload"),
  ISSUES_ATTACHMENT_DELETE("issues.attachment.delete"),
  MESSAGES_READ("messages.read"),
  MESSAGES_SEND("messages.send"),
  MESSAGES_THREAD_CREATE("messages.thread.create"),
  SCRUM_SPRINT_VIEW("scrum.sprint.view"),
  SCRUM_SPRINT_CREATE("scrum.sprint.create"),
  SCRUM_SPRINT_UPDATE("scrum.sprint.update"),
  SCRUM_SPRINT_COMPLETE("scrum.sprint.complete"),
  SCRUM_BACKLOG_VIEW("scrum.backlog.view"),
  SCRUM_BACKLOG_UPDATE("scrum.backlog.update"),
  KANBAN_BOARD_VIEW("kanban.board.view"),
  KANBAN_ISSUE_MOVE("kanban.issue.move"),
  OKR_OBJECTIVE_VIEW("okr.objective.view"),
  OKR_OBJECTIVE_CREATE("okr.objective.create"),
  OKR_OBJECTIVE_UPDATE("okr.objective.update"),
  OKR_OBJECTIVE_CLOSE("okr.objective.close"),
  OKR_KEY_RESULT_MANAGE("okr.key_result.manage"),
  GANTT_VIEW("gantt.view"),
  GANTT_UPDATE("gantt.update"),
  ANALYTICS_VIEW("analytics.view"),
  AUDIT_LOGS_VIEW("audit.logs.view"),
  ACCESS_LOGS_VIEW("access.logs.view"),
  METRICS_VIEW("metrics.view"),
  PUBLIC_API_KEYS_MANAGE("public.api.keys.manage"),
  INTEGRATIONS_GIT_MANAGE("integrations.git.manage"),
  TENANT_LIFECYCLE_MANAGE("tenant.lifecycle.manage");

  private final String code;

  PermissionCode(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }
}
