package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceIssueActivityRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceIssueActivityRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<IssueActivityRow> findByIssue(Long issueId) {
    String sql = """
        SELECT a.id,
               a.issue_id,
               a.actor_user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS actor_name,
               a.event_type,
               a.metadata,
               a.created_at
        FROM issue_activity_events a
        JOIN users u ON u.id = a.actor_user_id
        WHERE a.issue_id = ?
        ORDER BY a.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), issueId);
  }

  public Long insert(Long issueId, Long actorUserId, String eventType, String metadataJson) {
    String sql = """
        INSERT INTO issue_activity_events (issue_id, actor_user_id, event_type, metadata)
        VALUES (?, ?, ?, CAST(? AS jsonb))
        RETURNING id
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, rs -> rs.next() ? rs.getLong("id") : null,
        issueId, actorUserId, eventType, metadataJson);
  }

  private RowMapper<IssueActivityRow> mapper() {
    return (rs, rowNum) -> new IssueActivityRow(
        rs.getLong("id"),
        rs.getLong("issue_id"),
        rs.getLong("actor_user_id"),
        rs.getString("actor_name"),
        rs.getString("event_type"),
        rs.getString("metadata"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }

  public record IssueActivityRow(
      Long id,
      Long issueId,
      Long actorUserId,
      String actorName,
      String eventType,
      String metadataJson,
      LocalDateTime createdAt
  ) {}
}
