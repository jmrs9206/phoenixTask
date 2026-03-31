package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceIssueAttachmentRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceIssueAttachmentRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<IssueAttachmentRow> findByIssue(Long issueId) {
    String sql = """
        SELECT a.id,
               a.issue_id,
               a.uploader_user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS uploader_name,
               a.original_filename,
               a.stored_filename,
               a.mime_type,
               a.size_bytes,
               a.created_at
        FROM issue_attachments a
        JOIN users u ON u.id = a.uploader_user_id
        WHERE a.issue_id = ?
        ORDER BY a.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), issueId);
  }

  public Optional<IssueAttachmentRow> findById(Long attachmentId) {
    String sql = """
        SELECT a.id,
               a.issue_id,
               a.uploader_user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS uploader_name,
               a.original_filename,
               a.stored_filename,
               a.mime_type,
               a.size_bytes,
               a.created_at
        FROM issue_attachments a
        JOIN users u ON u.id = a.uploader_user_id
        WHERE a.id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), attachmentId).stream().findFirst();
  }

  public Long insert(
      Long issueId,
      Long uploaderUserId,
      String originalFilename,
      String storedFilename,
      String mimeType,
      long sizeBytes
  ) {
    String sql = """
        INSERT INTO issue_attachments
          (issue_id, uploader_user_id, original_filename, stored_filename, mime_type, size_bytes)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, rs -> rs.next() ? rs.getLong("id") : null,
        issueId, uploaderUserId, originalFilename, storedFilename, mimeType, sizeBytes);
  }

  public void delete(Long attachmentId) {
    String sql = "DELETE FROM issue_attachments WHERE id = ?";
    jdbcTemplateProvider.getJdbcTemplate().update(sql, attachmentId);
  }

  private RowMapper<IssueAttachmentRow> mapper() {
    return (rs, rowNum) -> new IssueAttachmentRow(
        rs.getLong("id"),
        rs.getLong("issue_id"),
        rs.getLong("uploader_user_id"),
        rs.getString("uploader_name"),
        rs.getString("original_filename"),
        rs.getString("stored_filename"),
        rs.getString("mime_type"),
        rs.getLong("size_bytes"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }

  public record IssueAttachmentRow(
      Long id,
      Long issueId,
      Long uploaderUserId,
      String uploaderName,
      String originalFilename,
      String storedFilename,
      String mimeType,
      long sizeBytes,
      LocalDateTime createdAt
  ) {}
}
