package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceAuthorizationRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceAuthorizationRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public Optional<Long> findTeamRoleId(Long teamId, Long userId) {
    String sql = """
        SELECT role_id
        FROM team_memberships
        WHERE team_id = ? AND user_id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("role_id"), teamId, userId).stream().findFirst();
  }

  public Optional<Long> findProjectRoleId(Long projectId, Long userId) {
    String sql = """
        SELECT role_id
        FROM project_memberships
        WHERE project_id = ? AND user_id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("role_id"), projectId, userId).stream().findFirst();
  }

  public Optional<Long> findProjectIdByIssue(Long issueId) {
    String sql = """
        SELECT project_id
        FROM issues
        WHERE id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("project_id"), issueId).stream().findFirst();
  }

  public Optional<Long> findProjectIdBySprintId(Long sprintId) {
    String sql = """
        SELECT project_id
        FROM sprints
        WHERE id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("project_id"), sprintId).stream().findFirst();
  }

  public Optional<Long> findProjectIdByObjective(Long objectiveId) {
    String sql = """
        SELECT project_id
        FROM okr_objectives
        WHERE id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("project_id"), objectiveId).stream().findFirst();
  }

  public boolean teamExists(Long teamId) {
    String sql = "SELECT 1 FROM teams WHERE id = ?";
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return !jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), teamId).isEmpty();
  }

  public boolean projectExists(Long projectId) {
    String sql = "SELECT 1 FROM projects WHERE id = ?";
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return !jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), projectId).isEmpty();
  }

  public Optional<MessageThreadContext> findThreadContext(Long threadId) {
    String sql = """
        SELECT thread_type, team_id, project_id, direct_user_one_id, direct_user_two_id
        FROM message_threads
        WHERE id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), threadId).stream().findFirst();
  }

  private RowMapper<MessageThreadContext> mapper() {
    return new RowMapper<>() {
      @Override
      public MessageThreadContext mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new MessageThreadContext(
            rs.getString("thread_type"),
            (Long) rs.getObject("team_id"),
            (Long) rs.getObject("project_id"),
            (Long) rs.getObject("direct_user_one_id"),
            (Long) rs.getObject("direct_user_two_id")
        );
      }
    };
  }

  public record MessageThreadContext(
      String threadType,
      Long teamId,
      Long projectId,
      Long directUserOneId,
      Long directUserTwoId
  ) {}
}
