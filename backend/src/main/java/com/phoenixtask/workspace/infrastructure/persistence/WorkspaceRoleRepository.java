package com.phoenixtask.workspace.infrastructure.persistence;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceRoleRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceRoleRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<RoleRow> findAssignable() {
    String sql = """
        SELECT id, code, name
        FROM roles
        WHERE is_assignable = true
        ORDER BY id
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper());
  }

  private RowMapper<RoleRow> mapper() {
    return (rs, rowNum) -> new RoleRow(
        rs.getLong("id"),
        rs.getString("code"),
        rs.getString("name")
    );
  }

  public record RoleRow(Long id, String code, String name) {}
}
