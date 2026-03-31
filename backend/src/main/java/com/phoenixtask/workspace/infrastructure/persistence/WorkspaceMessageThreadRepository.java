package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceMessageThreadRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceMessageThreadRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public boolean teamThreadMissingForUser(Long userId) {
    String sql = """
        SELECT 1
        FROM team_memberships tm
        JOIN teams t ON t.id = tm.team_id
        LEFT JOIN message_threads mt
          ON mt.thread_type = 'TEAM'
          AND mt.team_id = t.id
        WHERE tm.user_id = ?
          AND mt.id IS NULL
        LIMIT 1
        """;
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        userId
    );
    return value != null;
  }

  public boolean projectThreadMissingForUser(Long userId) {
    String sql = """
        SELECT 1
        FROM project_memberships pm
        JOIN projects p ON p.id = pm.project_id
        LEFT JOIN message_threads mt
          ON mt.thread_type = 'PROJECT'
          AND mt.project_id = p.id
        WHERE pm.user_id = ?
          AND mt.id IS NULL
        LIMIT 1
        """;
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        userId
    );
    return value != null;
  }

  public List<TeamThreadRow> findTeamThreads(Long userId) {
    String sql = """
        SELECT mt.id AS thread_id,
               t.id AS team_id,
               t.name AS team_name,
               mt.last_message_at
        FROM team_memberships tm
        JOIN teams t ON t.id = tm.team_id
        JOIN message_threads mt
          ON mt.thread_type = 'TEAM'
          AND mt.team_id = t.id
        WHERE tm.user_id = ?
        ORDER BY mt.last_message_at DESC NULLS LAST, mt.id ASC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, teamMapper(), userId);
  }

  public List<ProjectThreadRow> findProjectThreads(Long userId) {
    String sql = """
        SELECT mt.id AS thread_id,
               p.id AS project_id,
               p.project_key,
               p.name AS project_name,
               mt.last_message_at
        FROM project_memberships pm
        JOIN projects p ON p.id = pm.project_id
        JOIN message_threads mt
          ON mt.thread_type = 'PROJECT'
          AND mt.project_id = p.id
        WHERE pm.user_id = ?
        ORDER BY mt.last_message_at DESC NULLS LAST, mt.id ASC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, projectMapper(), userId);
  }

  public List<DirectThreadRow> findDirectThreads(Long userId) {
    String sql = """
        SELECT mt.id AS thread_id,
               CASE WHEN mt.direct_user_one_id = ? THEN u2.id ELSE u1.id END AS other_user_id,
               CASE WHEN mt.direct_user_one_id = ? THEN CONCAT(u2.first_name, ' ', u2.last_name)
                    ELSE CONCAT(u1.first_name, ' ', u1.last_name)
               END AS other_user_full_name,
               CASE WHEN mt.direct_user_one_id = ? THEN u2.email ELSE u1.email END AS other_user_email,
               mt.last_message_at
        FROM message_threads mt
        JOIN users u1 ON u1.id = mt.direct_user_one_id
        JOIN users u2 ON u2.id = mt.direct_user_two_id
        WHERE mt.thread_type = 'DIRECT'
          AND (mt.direct_user_one_id = ? OR mt.direct_user_two_id = ?)
        ORDER BY mt.last_message_at DESC NULLS LAST, mt.id ASC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        directMapper(),
        userId,
        userId,
        userId,
        userId,
        userId
    );
  }

  public ThreadRow findThreadById(Long threadId) {
    String sql = """
        SELECT id, company_id, thread_type, team_id, project_id,
               direct_user_one_id, direct_user_two_id, last_message_at
        FROM message_threads
        WHERE id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? new ThreadRow(
            rs.getLong("id"),
            rs.getLong("company_id"),
            rs.getString("thread_type"),
            (Long) rs.getObject("team_id"),
            (Long) rs.getObject("project_id"),
            (Long) rs.getObject("direct_user_one_id"),
            (Long) rs.getObject("direct_user_two_id"),
            rs.getTimestamp("last_message_at") != null
                ? rs.getTimestamp("last_message_at").toLocalDateTime()
                : null
        ) : null,
        threadId
    );
  }

  public Long createTeamThread(Long companyId, Long teamId) {
    String sql = """
        INSERT INTO message_threads (company_id, thread_type, team_id, created_at)
        VALUES (?, 'TEAM', ?, NOW())
        RETURNING id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        companyId,
        teamId
    );
  }

  public Long createProjectThread(Long companyId, Long projectId) {
    String sql = """
        INSERT INTO message_threads (company_id, thread_type, project_id, created_at)
        VALUES (?, 'PROJECT', ?, NOW())
        RETURNING id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        companyId,
        projectId
    );
  }

  public DirectThreadResultRow getOrCreateDirectThread(Long companyId, Long userOneId, Long userTwoId) {
    String insert = """
        INSERT INTO message_threads (
          company_id, thread_type, direct_user_one_id, direct_user_two_id, created_at
        )
        VALUES (?, 'DIRECT', ?, ?, NOW())
        ON CONFLICT (company_id, direct_user_one_id, direct_user_two_id) WHERE thread_type = 'DIRECT'
        DO UPDATE SET direct_user_one_id = message_threads.direct_user_one_id
        RETURNING id, direct_user_one_id, direct_user_two_id, last_message_at, (xmax = 0) AS created
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        insert,
        rs -> rs.next() ? new DirectThreadResultRow(
            rs.getLong("id"),
            rs.getLong("direct_user_one_id"),
            rs.getLong("direct_user_two_id"),
            rs.getTimestamp("last_message_at") != null
                ? rs.getTimestamp("last_message_at").toLocalDateTime()
                : null,
            rs.getBoolean("created")
        ) : null,
        companyId,
        userOneId,
        userTwoId
    );
  }

  private RowMapper<TeamThreadRow> teamMapper() {
    return (rs, rowNum) -> new TeamThreadRow(
        rs.getLong("thread_id"),
        rs.getLong("team_id"),
        rs.getString("team_name"),
        rs.getTimestamp("last_message_at") != null
            ? rs.getTimestamp("last_message_at").toLocalDateTime()
            : null
    );
  }

  private RowMapper<ProjectThreadRow> projectMapper() {
    return (rs, rowNum) -> new ProjectThreadRow(
        rs.getLong("thread_id"),
        rs.getLong("project_id"),
        rs.getString("project_key"),
        rs.getString("project_name"),
        rs.getTimestamp("last_message_at") != null
            ? rs.getTimestamp("last_message_at").toLocalDateTime()
            : null
    );
  }

  private RowMapper<DirectThreadRow> directMapper() {
    return (rs, rowNum) -> new DirectThreadRow(
        rs.getLong("thread_id"),
        rs.getLong("other_user_id"),
        rs.getString("other_user_full_name"),
        rs.getString("other_user_email"),
        rs.getTimestamp("last_message_at") != null
            ? rs.getTimestamp("last_message_at").toLocalDateTime()
            : null
    );
  }

  public record TeamThreadRow(
      Long threadId,
      Long teamId,
      String teamName,
      LocalDateTime lastMessageAt
  ) {}

  public record ProjectThreadRow(
      Long threadId,
      Long projectId,
      String projectKey,
      String projectName,
      LocalDateTime lastMessageAt
  ) {}

  public record DirectThreadRow(
      Long threadId,
      Long otherUserId,
      String otherUserFullName,
      String otherUserEmail,
      LocalDateTime lastMessageAt
  ) {}

  public record DirectThreadResultRow(
      Long threadId,
      Long directUserOneId,
      Long directUserTwoId,
      LocalDateTime lastMessageAt,
      boolean created
  ) {}

  public record ThreadRow(
      Long threadId,
      Long companyId,
      String threadType,
      Long teamId,
      Long projectId,
      Long directUserOneId,
      Long directUserTwoId,
      LocalDateTime lastMessageAt
  ) {}
}
