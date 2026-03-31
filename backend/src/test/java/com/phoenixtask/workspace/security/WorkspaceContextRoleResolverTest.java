package com.phoenixtask.workspace.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceAuthorizationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WorkspaceContextRoleResolverTest {

  @Test
  void companyScopeRequiresMatchingCompanyId() {
    WorkspaceAuthorizationRepository repository = new StubAuthorizationRepository();
    WorkspaceContextRoleResolver resolver = new WorkspaceContextRoleResolver(repository);
    AuthPrincipal principal = new AuthPrincipal(
        10L,
        "demo",
        "user@phoenixtask.demo",
        "Test",
        "User",
        "Test User",
        42L,
        7L,
        "ACTIVE",
        100L
    );

    assertTrue(resolver.resolveRoleId(principal, PermissionContext.company(42L)).isPresent());
    assertEquals(Optional.of(7L), resolver.resolveRoleId(principal, PermissionContext.company(42L)));
    assertTrue(resolver.resolveRoleId(principal, PermissionContext.company(99L)).isEmpty());
  }

  private static class StubAuthorizationRepository extends WorkspaceAuthorizationRepository {
    StubAuthorizationRepository() {
      super(null);
    }
  }
}
