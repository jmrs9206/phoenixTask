package com.phoenixtask.workspace.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceMemberRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceMemberRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<MemberRow> findTeamMembers(Long teamId) {
    String sql = """
        SELECT u.id AS user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS full_name,
               u.email,
               r.code AS role_code,
               r.name AS role_name,
               u.status
        FROM team_memberships tm
        JOIN users u ON u.id = tm.user_id
        JOIN roles r ON r.id = tm.role_id
        WHERE tm.team_id = ?
        ORDER BY u.last_name, u.first_name
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), teamId);
  }

  public List<MemberRow> findProjectMembers(Long projectId) {
    String sql = """
        SELECT u.id AS user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS full_name,
               u.email,
               r.code AS role_code,
               r.name AS role_name,
               u.status
        FROM project_memberships pm
        JOIN users u ON u.id = pm.user_id
        JOIN roles r ON r.id = pm.role_id
        WHERE pm.project_id = ?
        ORDER BY u.last_name, u.first_name
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), projectId);
  }

  public Boolean getRoleAssignability(Long roleId) {
    String sql = "SELECT is_assignable FROM roles WHERE id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getBoolean("is_assignable") : null,
        roleId
    );
  }

  public boolean teamMembershipExists(Long teamId, Long userId) {
    String sql = "SELECT 1 FROM team_memberships WHERE team_id = ? AND user_id = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        teamId,
        userId
    );
    return value != null;
  }

  public boolean projectMembershipExists(Long projectId, Long userId) {
    String sql = "SELECT 1 FROM project_memberships WHERE project_id = ? AND user_id = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        projectId,
        userId
    );
    return value != null;
  }

  public void insertTeamMembership(Long teamId, Long userId, Long roleId) {
    String sql = """
        INSERT INTO team_memberships (team_id, user_id, role_id, created_at)
        VALUES (?, ?, ?, NOW())
        """;
    jdbcTemplateProvider.getJdbcTemplate().update(sql, teamId, userId, roleId);
  }

  public void insertProjectMembership(Long projectId, Long userId, Long roleId) {
    String sql = """
        INSERT INTO project_memberships (project_id, user_id, role_id, created_at)
        VALUES (?, ?, ?, NOW())
        """;
    jdbcTemplateProvider.getJdbcTemplate().update(sql, projectId, userId, roleId);
  }

  private RowMapper<MemberRow> mapper() {
    return (rs, rowNum) -> new MemberRow(
        rs.getLong("user_id"),
        rs.getString("full_name"),
        rs.getString("email"),
        rs.getString("role_code"),
        rs.getString("role_name"),
        rs.getString("status")
    );
  }

  public record MemberRow(
      Long userId,
      String fullName,
      String email,
      String roleCode,
      String roleName,
      String status
  ) {}
}
