package com.phoenixtask.workspace.security;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class WorkspacePermissionMatrix {

  private static final List<PermissionDefinition> DEFINITIONS = List.of(
      new PermissionDefinition(PermissionModule.COMPANY, "view", PermissionCode.COMPANY_VIEW,
          "View company profile"),
      new PermissionDefinition(PermissionModule.COMPANY, "update", PermissionCode.COMPANY_UPDATE,
          "Update company profile"),
      new PermissionDefinition(PermissionModule.COMPANY, "settings.view", PermissionCode.COMPANY_SETTINGS_VIEW,
          "View company settings"),
      new PermissionDefinition(PermissionModule.COMPANY, "settings.update", PermissionCode.COMPANY_SETTINGS_UPDATE,
          "Update company settings"),
      new PermissionDefinition(PermissionModule.USERS, "read", PermissionCode.USERS_VIEW,
          "View users"),
      new PermissionDefinition(PermissionModule.USERS, "create", PermissionCode.USERS_CREATE,
          "Create users"),
      new PermissionDefinition(PermissionModule.USERS, "update", PermissionCode.USERS_UPDATE,
          "Update users"),
      new PermissionDefinition(PermissionModule.USERS, "deactivate", PermissionCode.USERS_DEACTIVATE,
          "Deactivate users"),
      new PermissionDefinition(PermissionModule.TEAMS, "read", PermissionCode.TEAMS_VIEW,
          "View teams"),
      new PermissionDefinition(PermissionModule.TEAMS, "create", PermissionCode.TEAMS_CREATE,
          "Create teams"),
      new PermissionDefinition(PermissionModule.TEAMS, "update", PermissionCode.TEAMS_UPDATE,
          "Update teams"),
      new PermissionDefinition(PermissionModule.TEAMS, "delete", PermissionCode.TEAMS_DELETE,
          "Delete teams"),
      new PermissionDefinition(PermissionModule.TEAMS, "members.view", PermissionCode.TEAM_MEMBERS_VIEW,
          "View team members"),
      new PermissionDefinition(PermissionModule.TEAMS, "members.add", PermissionCode.TEAM_MEMBERS_ADD,
          "Add team members"),
      new PermissionDefinition(PermissionModule.TEAMS, "members.remove", PermissionCode.TEAM_MEMBERS_REMOVE,
          "Remove team members"),
      new PermissionDefinition(PermissionModule.TEAMS, "members.role.update", PermissionCode.TEAM_MEMBERS_ROLE_UPDATE,
          "Update team member roles"),
      new PermissionDefinition(PermissionModule.PROJECTS, "read", PermissionCode.PROJECTS_VIEW,
          "View projects"),
      new PermissionDefinition(PermissionModule.PROJECTS, "create", PermissionCode.PROJECTS_CREATE,
          "Create projects"),
      new PermissionDefinition(PermissionModule.PROJECTS, "update", PermissionCode.PROJECTS_UPDATE,
          "Update projects"),
      new PermissionDefinition(PermissionModule.PROJECTS, "archive", PermissionCode.PROJECTS_ARCHIVE,
          "Archive projects"),
      new PermissionDefinition(PermissionModule.PROJECTS, "delete", PermissionCode.PROJECTS_DELETE,
          "Delete projects"),
      new PermissionDefinition(PermissionModule.PROJECTS, "members.view", PermissionCode.PROJECT_MEMBERS_VIEW,
          "View project members"),
      new PermissionDefinition(PermissionModule.PROJECTS, "members.add", PermissionCode.PROJECT_MEMBERS_ADD,
          "Add project members"),
      new PermissionDefinition(PermissionModule.PROJECTS, "members.remove", PermissionCode.PROJECT_MEMBERS_REMOVE,
          "Remove project members"),
      new PermissionDefinition(PermissionModule.PROJECTS, "members.role.update", PermissionCode.PROJECT_MEMBERS_ROLE_UPDATE,
          "Update project member roles"),
      new PermissionDefinition(PermissionModule.ISSUES, "read", PermissionCode.ISSUES_VIEW,
          "View issues"),
      new PermissionDefinition(PermissionModule.ISSUES, "create", PermissionCode.ISSUES_CREATE,
          "Create issues"),
      new PermissionDefinition(PermissionModule.ISSUES, "update", PermissionCode.ISSUES_UPDATE,
          "Update issues"),
      new PermissionDefinition(PermissionModule.ISSUES, "assign", PermissionCode.ISSUES_ASSIGN,
          "Assign issues"),
      new PermissionDefinition(PermissionModule.ISSUES, "transition", PermissionCode.ISSUES_TRANSITION,
          "Transition issues"),
      new PermissionDefinition(PermissionModule.ISSUES, "comments.view", PermissionCode.ISSUES_COMMENT_VIEW,
          "View issue comments"),
      new PermissionDefinition(PermissionModule.ISSUES, "comments.create", PermissionCode.ISSUES_COMMENT_CREATE,
          "Create issue comments"),
      new PermissionDefinition(PermissionModule.ISSUES, "comments.update", PermissionCode.ISSUES_COMMENT_UPDATE,
          "Update issue comments"),
      new PermissionDefinition(PermissionModule.ISSUES, "comments.delete", PermissionCode.ISSUES_COMMENT_DELETE,
          "Delete issue comments"),
      new PermissionDefinition(PermissionModule.ISSUES, "attachments.upload", PermissionCode.ISSUES_ATTACHMENT_UPLOAD,
          "Upload issue attachments"),
      new PermissionDefinition(PermissionModule.ISSUES, "attachments.delete", PermissionCode.ISSUES_ATTACHMENT_DELETE,
          "Delete issue attachments"),
      new PermissionDefinition(PermissionModule.MESSAGES, "read", PermissionCode.MESSAGES_READ,
          "Read messages"),
      new PermissionDefinition(PermissionModule.MESSAGES, "send", PermissionCode.MESSAGES_SEND,
          "Send messages"),
      new PermissionDefinition(PermissionModule.MESSAGES, "thread.create", PermissionCode.MESSAGES_THREAD_CREATE,
          "Create message threads"),
      new PermissionDefinition(PermissionModule.SCRUM, "sprint.view", PermissionCode.SCRUM_SPRINT_VIEW,
          "View sprints"),
      new PermissionDefinition(PermissionModule.SCRUM, "sprint.create", PermissionCode.SCRUM_SPRINT_CREATE,
          "Create sprints"),
      new PermissionDefinition(PermissionModule.SCRUM, "sprint.update", PermissionCode.SCRUM_SPRINT_UPDATE,
          "Update sprints"),
      new PermissionDefinition(PermissionModule.SCRUM, "sprint.complete", PermissionCode.SCRUM_SPRINT_COMPLETE,
          "Complete sprints"),
      new PermissionDefinition(PermissionModule.SCRUM, "backlog.view", PermissionCode.SCRUM_BACKLOG_VIEW,
          "View sprint backlog"),
      new PermissionDefinition(PermissionModule.SCRUM, "backlog.update", PermissionCode.SCRUM_BACKLOG_UPDATE,
          "Assign backlog items"),
      new PermissionDefinition(PermissionModule.KANBAN, "board.view", PermissionCode.KANBAN_BOARD_VIEW,
          "View kanban board"),
      new PermissionDefinition(PermissionModule.KANBAN, "issue.move", PermissionCode.KANBAN_ISSUE_MOVE,
          "Move issues on kanban"),
      new PermissionDefinition(PermissionModule.OKR, "objective.view", PermissionCode.OKR_OBJECTIVE_VIEW,
          "View OKR objectives"),
      new PermissionDefinition(PermissionModule.OKR, "objective.create", PermissionCode.OKR_OBJECTIVE_CREATE,
          "Create OKR objectives"),
      new PermissionDefinition(PermissionModule.OKR, "objective.update", PermissionCode.OKR_OBJECTIVE_UPDATE,
          "Update OKR objectives"),
      new PermissionDefinition(PermissionModule.OKR, "objective.close", PermissionCode.OKR_OBJECTIVE_CLOSE,
          "Close OKR objectives"),
      new PermissionDefinition(PermissionModule.OKR, "key_result.manage", PermissionCode.OKR_KEY_RESULT_MANAGE,
          "Manage OKR key results"),
      new PermissionDefinition(PermissionModule.GANTT, "view", PermissionCode.GANTT_VIEW,
          "View gantt"),
      new PermissionDefinition(PermissionModule.GANTT, "update", PermissionCode.GANTT_UPDATE,
          "Update gantt schedule"),
      new PermissionDefinition(PermissionModule.ANALYTICS, "view", PermissionCode.ANALYTICS_VIEW,
          "View analytics"),
      new PermissionDefinition(PermissionModule.PLATFORM, "roles.view", PermissionCode.ROLES_VIEW,
          "View roles"),
      new PermissionDefinition(PermissionModule.PLATFORM, "roles.assign", PermissionCode.ROLES_ASSIGN,
          "Assign roles"),
      new PermissionDefinition(PermissionModule.PLATFORM, "permissions.view", PermissionCode.PERMISSIONS_VIEW,
          "View permissions"),
      new PermissionDefinition(PermissionModule.PLATFORM, "public.api.keys.manage",
          PermissionCode.PUBLIC_API_KEYS_MANAGE, "Manage public API keys"),
      new PermissionDefinition(PermissionModule.PLATFORM, "integrations.git.manage",
          PermissionCode.INTEGRATIONS_GIT_MANAGE, "Manage Git integrations and repositories"),
      new PermissionDefinition(PermissionModule.PLATFORM, "tenant.lifecycle.manage",
          PermissionCode.TENANT_LIFECYCLE_MANAGE, "Manage tenant lifecycle")
  );

  private WorkspacePermissionMatrix() {}

  public static List<PermissionDefinition> definitions() {
    return DEFINITIONS;
  }

  public static Map<PermissionModule, List<PermissionDefinition>> byModule() {
    return DEFINITIONS.stream().collect(Collectors.groupingBy(PermissionDefinition::module));
  }

  public static Set<PermissionModule> modules() {
    return EnumSet.copyOf(byModule().keySet());
  }

  public static Set<PermissionCode> codes() {
    return DEFINITIONS.stream().map(PermissionDefinition::code).collect(Collectors.toSet());
  }
}
