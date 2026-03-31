package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.domain.TenantRegistry;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.shared.tenant.TenantCredentialMode;
import com.phoenixtask.workspace.infrastructure.config.WorkspaceProperties;
import org.springframework.stereotype.Component;

@Component
public class TenantCredentialResolver {

  private final WorkspaceProperties workspaceProperties;

  public TenantCredentialResolver(WorkspaceProperties workspaceProperties) {
    this.workspaceProperties = workspaceProperties;
  }

  public ResolvedCredentials resolve(TenantRegistry tenant) {
    String rawMode = tenant.getCredentialMode();
    TenantCredentialMode mode = TenantCredentialMode.from(rawMode)
        .orElse(TenantCredentialMode.SHARED);
    if (mode == TenantCredentialMode.SHARED) {
      String password = workspaceProperties.getDatasource().getPassword();
      if (password == null || password.isBlank()) {
        throw new ValidationException("Shared credential password is not configured");
      }
      return new ResolvedCredentials(
          tenant.getDbUsername(),
          password,
          mode.name(),
          "shared-workspace"
      );
    }
    return new ResolvedCredentials(
        tenant.getDbUsername(),
        null,
        mode.name(),
        "per-tenant"
    );
  }

  public record ResolvedCredentials(
      String username,
      String password,
      String mode,
      String source
  ) {}
}
