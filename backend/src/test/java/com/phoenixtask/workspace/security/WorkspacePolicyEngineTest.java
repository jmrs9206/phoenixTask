package com.phoenixtask.workspace.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.phoenixtask.security.AuthPrincipal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WorkspacePolicyEngineTest {

  @Test
  void policyEngineEvaluatesPermissionsByContext() {
    PermissionGrantProvider grants = roleId -> Map.of(
        1L, Set.of(PermissionCode.ISSUES_VIEW.code(), PermissionCode.MESSAGES_SEND.code()),
        2L, Set.of(PermissionCode.SCRUM_SPRINT_VIEW.code())
    ).getOrDefault(roleId, Set.of());

    ContextRoleResolver resolver = (principal, context) -> {
      if (context.scope() == PermissionScope.COMPANY) {
        if (context.companyId() != null && context.companyId().equals(1L)) {
          return Optional.of(1L);
        }
        return Optional.empty();
      }
      if (context.scope() == PermissionScope.PROJECT) {
        return Optional.of(2L);
      }
      return Optional.empty();
    };

    WorkspacePolicyEngine engine = new WorkspacePolicyEngine(grants, resolver);
    AuthPrincipal principal = new AuthPrincipal(
        10L,
        "demo",
        "user@phoenixtask.demo",
        "Test",
        "User",
        "Test User",
        1L,
        1L,
        "ACTIVE",
        99L
    );

    assertTrue(engine.isAllowed(principal, PermissionCode.ISSUES_VIEW, PermissionContext.company(1L)));
    assertFalse(engine.isAllowed(principal, PermissionCode.SCRUM_SPRINT_CREATE, PermissionContext.company(1L)));
    assertFalse(engine.isAllowed(principal, PermissionCode.ISSUES_VIEW, PermissionContext.company(2L)));
    assertTrue(engine.isAllowed(principal, PermissionCode.SCRUM_SPRINT_VIEW, PermissionContext.project(42L)));
  }

  @Test
  void permissionMatrixCoversAllModules() {
    Set<PermissionModule> modules = WorkspacePermissionMatrix.modules();
    assertTrue(modules.contains(PermissionModule.USERS));
    assertTrue(modules.contains(PermissionModule.ISSUES));
    assertTrue(modules.contains(PermissionModule.MESSAGES));
    assertTrue(modules.contains(PermissionModule.SCRUM));
    assertTrue(modules.contains(PermissionModule.KANBAN));
    assertTrue(modules.contains(PermissionModule.OKR));
    assertTrue(modules.contains(PermissionModule.GANTT));
    assertTrue(modules.contains(PermissionModule.ANALYTICS));
    assertTrue(modules.contains(PermissionModule.PLATFORM));
  }
}
