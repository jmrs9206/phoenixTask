package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceIssueCodeLinkRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceIssueCodeLinkRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public Optional<IssueCodeLinkRow> insertIfAbsent(
      Long issueId,
      Long projectId,
      Long integrationId,
      Long repoId,
      String provider,
      String artifactType,
      String externalId,
      String title,
      String url,
      String authorName,
      Instant externalCreatedAt
  ) {
    String sql = """
        INSERT INTO issue_code_links
          (issue_id, project_id, integration_id, repo_id, provider, artifact_type, external_id, title, url,
           author_name, external_created_at, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        ON CONFLICT (issue_id, repo_id, artifact_type, external_id) DO NOTHING
        RETURNING id, issue_id, project_id, integration_id, repo_id, provider, artifact_type, external_id, title, url,
                  author_name, external_created_at, created_at, updated_at
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(
        sql,
        mapper(),
        issueId,
        projectId,
        integrationId,
        repoId,
        provider,
        artifactType,
        externalId,
        title,
        url,
        authorName,
        externalCreatedAt != null ? Timestamp.from(externalCreatedAt) : null
    ).stream().findFirst();
  }

  public List<IssueCodeLinkRow> findByIssue(Long issueId) {
    String sql = """
        SELECT l.id, l.issue_id, l.project_id, l.integration_id, l.repo_id, l.provider, l.artifact_type, l.external_id,
               l.title, l.url, l.author_name, l.external_created_at, l.created_at, l.updated_at,
               r.repo_owner, r.repo_name
        FROM issue_code_links l
        JOIN git_repositories r ON r.id = l.repo_id
        WHERE l.issue_id = ?
        ORDER BY COALESCE(l.external_created_at, l.created_at) DESC, l.id DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, listMapper(), issueId);
  }

  private RowMapper<IssueCodeLinkRow> mapper() {
    return (rs, rowNum) -> new IssueCodeLinkRow(
        rs.getLong("id"),
        rs.getLong("issue_id"),
        rs.getLong("project_id"),
        rs.getLong("integration_id"),
        rs.getLong("repo_id"),
        rs.getString("provider"),
        rs.getString("artifact_type"),
        rs.getString("external_id"),
        rs.getString("title"),
        rs.getString("url"),
        rs.getString("author_name"),
        rs.getTimestamp("external_created_at") != null
            ? rs.getTimestamp("external_created_at").toInstant()
            : null,
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime(),
        null,
        null
    );
  }

  private RowMapper<IssueCodeLinkRow> listMapper() {
    return (rs, rowNum) -> new IssueCodeLinkRow(
        rs.getLong("id"),
        rs.getLong("issue_id"),
        rs.getLong("project_id"),
        rs.getLong("integration_id"),
        rs.getLong("repo_id"),
        rs.getString("provider"),
        rs.getString("artifact_type"),
        rs.getString("external_id"),
        rs.getString("title"),
        rs.getString("url"),
        rs.getString("author_name"),
        rs.getTimestamp("external_created_at") != null
            ? rs.getTimestamp("external_created_at").toInstant()
            : null,
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime(),
        rs.getString("repo_owner"),
        rs.getString("repo_name")
    );
  }

  public record IssueCodeLinkRow(
      Long id,
      Long issueId,
      Long projectId,
      Long integrationId,
      Long repoId,
      String provider,
      String artifactType,
      String externalId,
      String title,
      String url,
      String authorName,
      Instant externalCreatedAt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      String repoOwner,
      String repoName
  ) {}
}
