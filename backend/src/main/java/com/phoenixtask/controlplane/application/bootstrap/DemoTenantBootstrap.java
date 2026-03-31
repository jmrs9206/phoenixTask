package com.phoenixtask.controlplane.application.bootstrap;

import com.phoenixtask.controlplane.application.TenantRegistryService;
import com.phoenixtask.controlplane.domain.TenantRegistry;
import com.phoenixtask.shared.tenant.TenantCredentialMode;
import com.phoenixtask.workspace.application.bootstrap.WorkspaceDemoBootstrap;
import com.phoenixtask.workspace.infrastructure.config.WorkspaceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "phoenixtask.bootstrap.demo.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class DemoTenantBootstrap implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoTenantBootstrap.class);

  private final TenantRegistryService tenantRegistryService;
  private final WorkspaceProperties workspaceProperties;
  private final WorkspaceDemoBootstrap workspaceDemoBootstrap;

  public DemoTenantBootstrap(
      TenantRegistryService tenantRegistryService,
      WorkspaceProperties workspaceProperties,
      WorkspaceDemoBootstrap workspaceDemoBootstrap
  ) {
    this.tenantRegistryService = tenantRegistryService;
    this.workspaceProperties = workspaceProperties;
    this.workspaceDemoBootstrap = workspaceDemoBootstrap;
  }

  @Override
  public void run(ApplicationArguments args) {
    ensureDemoTenantRegistered();
    workspaceDemoBootstrap.bootstrapDemoWorkspace();
  }

  private void ensureDemoTenantRegistered() {
    if (tenantRegistryService.findByCode("demo").isPresent()) {
      log.info("Demo tenant already registered");
      return;
    }

    TenantRegistry demo = new TenantRegistry();
    demo.setCode("demo");
    demo.setName("PhoenixTask® Demo Workspace");
    demo.setDbHost(workspaceProperties.getDatasource().getHost());
    demo.setDbPort(workspaceProperties.getDatasource().getPort());
    demo.setDbName(workspaceProperties.getDatasource().getDatabase());
    demo.setDbSchema(workspaceProperties.getDatasource().getSchema());
    demo.setDbUsername(workspaceProperties.getDatasource().getUsername());
    demo.setCredentialMode(TenantCredentialMode.SHARED.name());
    demo.setStatus("ACTIVE");

    tenantRegistryService.save(demo);
    log.info("Demo tenant registered in tenant_registry");
  }
}
