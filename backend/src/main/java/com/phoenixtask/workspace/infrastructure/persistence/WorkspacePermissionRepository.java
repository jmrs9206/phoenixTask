package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import com.phoenixtask.workspace.security.PermissionGrantProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspacePermissionRepository implements PermissionGrantProvider {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspacePermissionRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  @Override
  public Set<String> findPermissionCodesByRoleId(Long roleId) {
    String sql = """
        SELECT p.code
        FROM role_permissions rp
        JOIN permissions p ON p.id = rp.permission_id
        WHERE rp.role_id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("code"), roleId);
    return new HashSet<>(rows);
  }
}
