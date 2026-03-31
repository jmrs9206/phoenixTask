package com.phoenixtask.workspace.security;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceAuthorizationRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceAuthorizationRepository.MessageThreadContext;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceContextRoleResolver implements ContextRoleResolver {

  private final WorkspaceAuthorizationRepository authorizationRepository;

  public WorkspaceContextRoleResolver(WorkspaceAuthorizationRepository authorizationRepository) {
    this.authorizationRepository = authorizationRepository;
  }

  @Override
  public Optional<Long> resolveRoleId(AuthPrincipal principal, PermissionContext context) {
    if (context == null || context.scope() == null) {
      return Optional.empty();
    }
    return switch (context.scope()) {
      case COMPANY -> resolveCompanyRole(principal, context.companyId());
      case TEAM -> resolveTeamRole(principal, context.teamId());
      case PROJECT -> resolveProjectRole(principal, context.projectId());
      case ISSUE -> resolveIssueRole(principal, context.issueId());
      case THREAD -> resolveThreadRole(principal, context.threadId());
    };
  }

  private Optional<Long> resolveCompanyRole(AuthPrincipal principal, Long companyId) {
    if (companyId == null || principal.getCompanyId() == null) {
      return Optional.empty();
    }
    if (!companyId.equals(principal.getCompanyId())) {
      return Optional.empty();
    }
    return Optional.ofNullable(principal.getPrimaryRoleId());
  }

  private Optional<Long> resolveTeamRole(AuthPrincipal principal, Long teamId) {
    if (teamId == null) {
      return Optional.empty();
    }
    return authorizationRepository.findTeamRoleId(teamId, principal.getUserId());
  }

  private Optional<Long> resolveProjectRole(AuthPrincipal principal, Long projectId) {
    if (projectId == null) {
      return Optional.empty();
    }
    return authorizationRepository.findProjectRoleId(projectId, principal.getUserId());
  }

  private Optional<Long> resolveIssueRole(AuthPrincipal principal, Long issueId) {
    if (issueId == null) {
      return Optional.empty();
    }
    Optional<Long> projectId = authorizationRepository.findProjectIdByIssue(issueId);
    return projectId.flatMap(id -> authorizationRepository.findProjectRoleId(id, principal.getUserId()));
  }

  private Optional<Long> resolveThreadRole(AuthPrincipal principal, Long threadId) {
    if (threadId == null) {
      return Optional.empty();
    }
    Optional<MessageThreadContext> context = authorizationRepository.findThreadContext(threadId);
    if (context.isEmpty()) {
      return Optional.empty();
    }
    MessageThreadContext row = context.get();
    if ("DIRECT".equalsIgnoreCase(row.threadType())) {
      if (principal.getUserId().equals(row.directUserOneId()) || principal.getUserId().equals(row.directUserTwoId())) {
        return Optional.ofNullable(principal.getPrimaryRoleId());
      }
      return Optional.empty();
    }
    if ("TEAM".equalsIgnoreCase(row.threadType())) {
      return resolveTeamRole(principal, row.teamId());
    }
    if ("PROJECT".equalsIgnoreCase(row.threadType())) {
      return resolveProjectRole(principal, row.projectId());
    }
    return Optional.empty();
  }
}
