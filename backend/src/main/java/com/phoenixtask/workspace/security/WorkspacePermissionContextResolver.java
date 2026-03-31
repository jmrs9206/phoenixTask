package com.phoenixtask.workspace.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceAuthorizationRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import com.phoenixtask.shared.interfaces.CachedBodyHttpServletRequest;

@Component
public class WorkspacePermissionContextResolver {

  private final WorkspaceAuthorizationRepository authorizationRepository;
  private final ObjectMapper objectMapper;

  public WorkspacePermissionContextResolver(
      WorkspaceAuthorizationRepository authorizationRepository,
      ObjectMapper objectMapper
  ) {
    this.authorizationRepository = authorizationRepository;
    this.objectMapper = objectMapper;
  }

  public ContextResolution resolve(
      EndpointPermissionRule rule,
      AuthPrincipal principal,
      HttpServletRequest request
  ) {
    return switch (rule.resolverType()) {
      case COMPANY -> resolveCompany(principal);
      case TEAM_PATH -> resolveTeam(pathVariable(request, rule.contextKey()));
      case PROJECT_PATH -> resolveProject(pathVariable(request, rule.contextKey()));
      case ISSUE_PATH -> resolveIssue(pathVariable(request, rule.contextKey()));
      case THREAD_PATH -> resolveThread(pathVariable(request, rule.contextKey()));
      case PROJECT_FROM_SPRINT_PATH -> resolveProjectFromSprint(pathVariable(request, rule.contextKey()));
      case PROJECT_FROM_OBJECTIVE_PATH -> resolveProjectFromObjective(pathVariable(request, rule.contextKey()));
      case PROJECT_FROM_BODY -> resolveProject(bodyFieldAsLong(request, rule.contextKey()));
      case ISSUE_FROM_BODY -> resolveIssue(bodyFieldAsLong(request, rule.contextKey()));
    };
  }

  private ContextResolution resolveCompany(AuthPrincipal principal) {
    if (principal == null || principal.getCompanyId() == null) {
      return ContextResolution.notFound();
    }
    return ContextResolution.ok(PermissionContext.company(principal.getCompanyId()));
  }

  private ContextResolution resolveTeam(Long teamId) {
    if (teamId == null) {
      throw new ValidationException("teamId is required");
    }
    if (!authorizationRepository.teamExists(teamId)) {
      return ContextResolution.notFound();
    }
    return ContextResolution.ok(PermissionContext.team(teamId));
  }

  private ContextResolution resolveProject(Long projectId) {
    if (projectId == null) {
      throw new ValidationException("projectId is required");
    }
    if (!authorizationRepository.projectExists(projectId)) {
      return ContextResolution.notFound();
    }
    return ContextResolution.ok(PermissionContext.project(projectId));
  }

  private ContextResolution resolveIssue(Long issueId) {
    if (issueId == null) {
      throw new ValidationException("issueId is required");
    }
    Optional<Long> projectId = authorizationRepository.findProjectIdByIssue(issueId);
    if (projectId.isEmpty()) {
      return ContextResolution.notFound();
    }
    return ContextResolution.ok(PermissionContext.issue(issueId));
  }

  private ContextResolution resolveThread(Long threadId) {
    if (threadId == null) {
      throw new ValidationException("threadId is required");
    }
    if (authorizationRepository.findThreadContext(threadId).isEmpty()) {
      return ContextResolution.notFound();
    }
    return ContextResolution.ok(PermissionContext.thread(threadId));
  }

  private ContextResolution resolveProjectFromSprint(Long sprintId) {
    if (sprintId == null) {
      throw new ValidationException("sprintId is required");
    }
    Optional<Long> projectId = authorizationRepository.findProjectIdBySprintId(sprintId);
    if (projectId.isEmpty()) {
      return ContextResolution.notFound();
    }
    return ContextResolution.ok(PermissionContext.project(projectId.get()));
  }

  private ContextResolution resolveProjectFromObjective(Long objectiveId) {
    if (objectiveId == null) {
      throw new ValidationException("objectiveId is required");
    }
    Optional<Long> projectId = authorizationRepository.findProjectIdByObjective(objectiveId);
    if (projectId.isEmpty()) {
      return ContextResolution.notFound();
    }
    return ContextResolution.ok(PermissionContext.project(projectId.get()));
  }

  private Long pathVariable(HttpServletRequest request, String key) {
    if (key == null) {
      return null;
    }
    Object vars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (!(vars instanceof Map<?, ?> map)) {
      return null;
    }
    Object raw = map.get(key);
    if (raw == null) {
      return null;
    }
    try {
      long value = Long.parseLong(raw.toString());
      return value > 0 ? value : null;
    } catch (NumberFormatException ex) {
      throw new ValidationException(key + " must be a number");
    }
  }

  private Long bodyFieldAsLong(HttpServletRequest request, String field) {
    if (field == null) {
      return null;
    }
    if (!(request instanceof CachedBodyHttpServletRequest wrapper)) {
      throw new ValidationException("Request body caching not enabled");
    }
    byte[] body = wrapper.getCachedBody();
    if (body == null || body.length == 0) {
      throw new ValidationException(field + " is required");
    }
    try {
      Map<String, Object> payload = objectMapper.readValue(
          new String(body, StandardCharsets.UTF_8),
          new TypeReference<>() {}
      );
      Object value = payload.get(field);
      if (value == null) {
        throw new ValidationException(field + " is required");
      }
      if (value instanceof Number number) {
        return number.longValue();
      }
      long parsed = Long.parseLong(value.toString());
      return parsed > 0 ? parsed : null;
    } catch (NumberFormatException ex) {
      throw new ValidationException(field + " must be a number");
    } catch (Exception ex) {
      throw new ValidationException("Invalid request body");
    }
  }
}
