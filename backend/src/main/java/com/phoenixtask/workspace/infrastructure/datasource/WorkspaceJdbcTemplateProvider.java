package com.phoenixtask.workspace.infrastructure.datasource;

import com.phoenixtask.workspace.infrastructure.tenant.TenantContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceJdbcTemplateProvider {

  private final WorkspaceDataSourceProvider dataSourceProvider;
  private final Map<String, JdbcTemplate> cache = new ConcurrentHashMap<>();

  public WorkspaceJdbcTemplateProvider(WorkspaceDataSourceProvider dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  public JdbcTemplate getJdbcTemplate() {
    String tenantCode = TenantContext.getRequired().getTenantCode();
    return cache.computeIfAbsent(tenantCode, code -> new JdbcTemplate(dataSourceProvider.getDataSource()));
  }
}
