package com.phoenixtask.workspace.security;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class WorkspaceEndpointPermissionRegistry {

  private final AntPathMatcher matcher = new AntPathMatcher();

  private final List<EndpointPermissionRule> rules = List.of(
      // Company & users
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/company", PermissionCode.COMPANY_VIEW,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/users", PermissionCode.USERS_VIEW,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/users/roles", PermissionCode.ROLES_VIEW,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/permissions/me", PermissionCode.PERMISSIONS_VIEW,
          ContextResolverType.COMPANY, null),

      // Teams
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/teams", PermissionCode.TEAMS_VIEW,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/teams", PermissionCode.TEAMS_CREATE,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/teams/{teamId}/members", PermissionCode.TEAM_MEMBERS_VIEW,
          ContextResolverType.TEAM_PATH, "teamId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/teams/{teamId}/members", PermissionCode.TEAM_MEMBERS_ADD,
          ContextResolverType.TEAM_PATH, "teamId"),

      // Projects
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/projects", PermissionCode.PROJECTS_VIEW,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/projects", PermissionCode.PROJECTS_CREATE,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/projects/{projectId}/members", PermissionCode.PROJECT_MEMBERS_VIEW,
          ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/projects/{projectId}/members", PermissionCode.PROJECT_MEMBERS_ADD,
          ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/projects/{projectId}/issues", PermissionCode.ISSUES_VIEW,
          ContextResolverType.PROJECT_PATH, "projectId"),

      // Issues
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/issues", PermissionCode.ISSUES_VIEW,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/issues/{issueId}", PermissionCode.ISSUES_VIEW,
          ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/issues/{issueId}/activity", PermissionCode.ISSUES_VIEW,
          ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/issues", PermissionCode.ISSUES_CREATE,
          ContextResolverType.PROJECT_FROM_BODY, "projectId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/issues/{issueId}/comments", PermissionCode.ISSUES_COMMENT_VIEW,
          ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/issues/{issueId}/comments", PermissionCode.ISSUES_COMMENT_CREATE,
          ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.DELETE, "/api/workspace/issues/{issueId}/comments/{commentId}",
          PermissionCode.ISSUES_COMMENT_DELETE, ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/issues/{issueId}/attachments", PermissionCode.ISSUES_VIEW,
          ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/issues/{issueId}/attachments", PermissionCode.ISSUES_ATTACHMENT_UPLOAD,
          ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/issues/{issueId}/attachments/{attachmentId}/download",
          PermissionCode.ISSUES_VIEW, ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/issues/{issueId}/attachments/{attachmentId}/preview",
          PermissionCode.ISSUES_VIEW, ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.DELETE, "/api/workspace/issues/{issueId}/attachments/{attachmentId}",
          PermissionCode.ISSUES_ATTACHMENT_DELETE, ContextResolverType.ISSUE_PATH, "issueId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/issues/{issueId}/code-links",
          PermissionCode.ISSUES_VIEW, ContextResolverType.ISSUE_PATH, "issueId"),

      // Messages
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/messages/teams", PermissionCode.MESSAGES_READ,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/messages/projects", PermissionCode.MESSAGES_READ,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/messages/direct", PermissionCode.MESSAGES_READ,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/messages/threads/{threadId}/messages", PermissionCode.MESSAGES_READ,
          ContextResolverType.THREAD_PATH, "threadId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/messages/threads/{threadId}/messages", PermissionCode.MESSAGES_SEND,
          ContextResolverType.THREAD_PATH, "threadId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/messages/direct", PermissionCode.MESSAGES_THREAD_CREATE,
          ContextResolverType.COMPANY, null),

      // Scrum
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/scrum/projects/{projectId}/sprints", PermissionCode.SCRUM_SPRINT_VIEW,
          ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/scrum/projects/{projectId}/sprints", PermissionCode.SCRUM_SPRINT_CREATE,
          ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/scrum/sprints/{sprintId}", PermissionCode.SCRUM_SPRINT_UPDATE,
          ContextResolverType.PROJECT_FROM_SPRINT_PATH, "sprintId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/scrum/projects/{projectId}/backlog", PermissionCode.SCRUM_BACKLOG_VIEW,
          ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/scrum/sprints/{sprintId}/issues", PermissionCode.SCRUM_BACKLOG_VIEW,
          ContextResolverType.PROJECT_FROM_SPRINT_PATH, "sprintId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/scrum/sprints/{sprintId}/health", PermissionCode.SCRUM_SPRINT_VIEW,
          ContextResolverType.PROJECT_FROM_SPRINT_PATH, "sprintId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/scrum/sprints/{sprintId}/issues", PermissionCode.SCRUM_BACKLOG_UPDATE,
          ContextResolverType.PROJECT_FROM_SPRINT_PATH, "sprintId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/scrum/sprints/backlog/issues", PermissionCode.SCRUM_BACKLOG_UPDATE,
          ContextResolverType.ISSUE_FROM_BODY, "issueId"),

      // OKR
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/okr/projects/{projectId}/objectives",
          PermissionCode.OKR_OBJECTIVE_VIEW, ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/okr/projects/{projectId}/objectives/details",
          PermissionCode.OKR_OBJECTIVE_VIEW, ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/okr/projects/{projectId}/objectives",
          PermissionCode.OKR_OBJECTIVE_CREATE, ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/okr/objectives/{objectiveId}",
          PermissionCode.OKR_OBJECTIVE_VIEW, ContextResolverType.PROJECT_FROM_OBJECTIVE_PATH, "objectiveId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/okr/objectives/{objectiveId}/key-results",
          PermissionCode.OKR_KEY_RESULT_MANAGE, ContextResolverType.PROJECT_FROM_OBJECTIVE_PATH, "objectiveId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/okr/objectives/{objectiveId}/check-ins",
          PermissionCode.OKR_OBJECTIVE_UPDATE, ContextResolverType.PROJECT_FROM_OBJECTIVE_PATH, "objectiveId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/okr/objectives/{objectiveId}/initiatives",
          PermissionCode.OKR_OBJECTIVE_UPDATE, ContextResolverType.PROJECT_FROM_OBJECTIVE_PATH, "objectiveId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/okr/objectives/{objectiveId}/close",
          PermissionCode.OKR_OBJECTIVE_CLOSE, ContextResolverType.PROJECT_FROM_OBJECTIVE_PATH, "objectiveId"),

      // Kanban
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/kanban/projects", PermissionCode.KANBAN_BOARD_VIEW,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/kanban/projects/{projectId}", PermissionCode.KANBAN_BOARD_VIEW,
          ContextResolverType.PROJECT_PATH, "projectId"),

      // Gantt
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/gantt/projects", PermissionCode.GANTT_VIEW,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/gantt/projects/{projectId}", PermissionCode.GANTT_VIEW,
          ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/gantt/projects/{projectId}/baseline", PermissionCode.GANTT_UPDATE,
          ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/gantt/projects/{projectId}/dependencies", PermissionCode.GANTT_UPDATE,
          ContextResolverType.PROJECT_PATH, "projectId"),

      // Analytics
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/analytics/summary", PermissionCode.ANALYTICS_VIEW,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/analytics/advanced", PermissionCode.ANALYTICS_VIEW,
          ContextResolverType.COMPANY, null),
      // Integrations (Git)
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/integrations/git", PermissionCode.INTEGRATIONS_GIT_MANAGE,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/integrations/git", PermissionCode.INTEGRATIONS_GIT_MANAGE,
          ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/integrations/git/{integrationId}/revoke",
          PermissionCode.INTEGRATIONS_GIT_MANAGE, ContextResolverType.COMPANY, null),
      new EndpointPermissionRule(HttpMethod.POST, "/api/workspace/integrations/git/{integrationId}/projects/{projectId}/repos",
          PermissionCode.INTEGRATIONS_GIT_MANAGE, ContextResolverType.PROJECT_PATH, "projectId"),
      new EndpointPermissionRule(HttpMethod.GET, "/api/workspace/integrations/git/{integrationId}/repos",
          PermissionCode.INTEGRATIONS_GIT_MANAGE, ContextResolverType.COMPANY, null)
  );

  public Optional<EndpointPermissionRule> match(String method, String path) {
    HttpMethod httpMethod;
    try {
      httpMethod = HttpMethod.valueOf(method);
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
    return rules.stream()
        .filter(rule -> rule.method() == httpMethod && matcher.match(rule.pathPattern(), path))
        .findFirst();
  }

  public List<EndpointPermissionRule> listRules() {
    return rules;
  }
}
