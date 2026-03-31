package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceMessageRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceMessageRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<MessageRow> findMessages(Long threadId) {
    String sql = """
        SELECT m.id AS message_id,
               m.author_user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS author_full_name,
               m.body,
               m.created_at
        FROM messages m
        JOIN users u ON u.id = m.author_user_id
        WHERE m.thread_id = ?
        ORDER BY m.created_at ASC, m.id ASC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), threadId);
  }

  public MessageInsertRow insertMessage(Long threadId, Long authorUserId, String body) {
    String sql = """
        WITH inserted AS (
          INSERT INTO messages (thread_id, author_user_id, body, created_at)
          VALUES (?, ?, ?, NOW())
          RETURNING id, created_at
        )
        UPDATE message_threads
        SET last_message_at = (SELECT created_at FROM inserted)
        WHERE id = ?
        RETURNING (SELECT id FROM inserted) AS message_id,
                  (SELECT created_at FROM inserted) AS created_at
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? new MessageInsertRow(
            rs.getLong("message_id"),
            rs.getTimestamp("created_at").toLocalDateTime()
        ) : null,
        threadId,
        authorUserId,
        body,
        threadId
    );
  }

  private RowMapper<MessageRow> mapper() {
    return (rs, rowNum) -> new MessageRow(
        rs.getLong("message_id"),
        rs.getLong("author_user_id"),
        rs.getString("author_full_name"),
        rs.getString("body"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }

  public record MessageRow(
      Long messageId,
      Long authorUserId,
      String authorFullName,
      String body,
      LocalDateTime createdAt
  ) {}

  public record MessageInsertRow(
      Long messageId,
      LocalDateTime createdAt
  ) {}
}
