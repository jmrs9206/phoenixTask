package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceAuthRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceAuthRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public Optional<AuthUserRecord> findByEmail(String email) {
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(
        """
        SELECT id, company_id, primary_role_id, first_name, last_name, email, status, password_hash
        FROM users
        WHERE LOWER(email) = LOWER(?)
        LIMIT 1
        """,
        new AuthUserRowMapper(),
        email
    ).stream().findFirst();
  }

  public Optional<AuthUserRecord> findById(Long id) {
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(
        """
        SELECT id, company_id, primary_role_id, first_name, last_name, email, status, password_hash
        FROM users
        WHERE id = ?
        LIMIT 1
        """,
        new AuthUserRowMapper(),
        id
    ).stream().findFirst();
  }

  public record AuthUserRecord(
      Long id,
      Long companyId,
      Long primaryRoleId,
      String firstName,
      String lastName,
      String email,
      String status,
      String passwordHash
  ) {}

  private static class AuthUserRowMapper implements RowMapper<AuthUserRecord> {
    @Override
    public AuthUserRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new AuthUserRecord(
          rs.getLong("id"),
          rs.getLong("company_id"),
          rs.getLong("primary_role_id"),
          rs.getString("first_name"),
          rs.getString("last_name"),
          rs.getString("email"),
          rs.getString("status"),
          rs.getString("password_hash")
      );
    }
  }
}
