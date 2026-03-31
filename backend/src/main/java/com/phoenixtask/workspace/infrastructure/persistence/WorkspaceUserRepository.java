package com.phoenixtask.workspace.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceUserRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceUserRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<UserRow> findAll() {
    String sql = """
        SELECT id, company_id, primary_role_id, first_name, last_name, email, status, created_at, updated_at
        FROM users
        ORDER BY id
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper());
  }

  public boolean existsById(Long userId) {
    String sql = "SELECT 1 FROM users WHERE id = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        userId
    );
    return value != null;
  }

  public UserSummaryRow findSummaryById(Long userId) {
    String sql = """
        SELECT id, company_id, first_name, last_name, email, status
        FROM users
        WHERE id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? new UserSummaryRow(
            rs.getLong("id"),
            rs.getLong("company_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("status")
        ) : null,
        userId
    );
  }

  private RowMapper<UserRow> mapper() {
    return (rs, rowNum) -> new UserRow(
        rs.getLong("id"),
        rs.getLong("company_id"),
        rs.getLong("primary_role_id"),
        rs.getString("first_name"),
        rs.getString("last_name"),
        rs.getString("email"),
        rs.getString("status"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime()
    );
  }

  public record UserRow(
      Long id,
      Long companyId,
      Long primaryRoleId,
      String firstName,
      String lastName,
      String email,
      String status,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}

  public record UserSummaryRow(
      Long id,
      Long companyId,
      String firstName,
      String lastName,
      String email,
      String status
  ) {}
}
