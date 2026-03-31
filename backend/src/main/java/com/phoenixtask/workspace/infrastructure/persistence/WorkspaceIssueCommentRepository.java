package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceIssueCommentRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceIssueCommentRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<IssueCommentRow> findByIssue(Long issueId) {
    String sql = """
        SELECT c.id,
               c.issue_id,
               c.author_user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS author_name,
               c.body,
               c.created_at
        FROM issue_comments c
        JOIN users u ON u.id = c.author_user_id
        WHERE c.issue_id = ?
        ORDER BY c.created_at ASC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), issueId);
  }

  public Optional<IssueCommentRow> findById(Long commentId) {
    String sql = """
        SELECT c.id,
               c.issue_id,
               c.author_user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS author_name,
               c.body,
               c.created_at
        FROM issue_comments c
        JOIN users u ON u.id = c.author_user_id
        WHERE c.id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), commentId).stream().findFirst();
  }

  public Long insert(Long issueId, Long authorUserId, String body) {
    String sql = """
        INSERT INTO issue_comments (issue_id, author_user_id, body)
        VALUES (?, ?, ?)
        RETURNING id
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, rs -> rs.next() ? rs.getLong("id") : null, issueId, authorUserId, body);
  }

  public void delete(Long commentId) {
    String sql = "DELETE FROM issue_comments WHERE id = ?";
    jdbcTemplateProvider.getJdbcTemplate().update(sql, commentId);
  }

  private RowMapper<IssueCommentRow> mapper() {
    return (rs, rowNum) -> new IssueCommentRow(
        rs.getLong("id"),
        rs.getLong("issue_id"),
        rs.getLong("author_user_id"),
        rs.getString("author_name"),
        rs.getString("body"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }

  public record IssueCommentRow(
      Long id,
      Long issueId,
      Long authorUserId,
      String authorName,
      String body,
      LocalDateTime createdAt
  ) {}
}
