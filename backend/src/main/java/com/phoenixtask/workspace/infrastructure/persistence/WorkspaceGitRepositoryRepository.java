package com.phoenixtask.workspace.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceGitRepositoryRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceGitRepositoryRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public GitRepositoryRow insert(
      Long integrationId,
      Long projectId,
      String repoOwner,
      String repoName,
      String defaultBranch,
      String status
  ) {
    String sql = """
        INSERT INTO git_repositories
          (integration_id, project_id, repo_owner, repo_name, default_branch, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
        RETURNING id, integration_id, project_id, repo_owner, repo_name, default_branch, status, created_at, updated_at
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), integrationId, projectId, repoOwner, repoName, defaultBranch, status)
        .stream()
        .findFirst()
        .orElse(null);
  }

  public Optional<GitRepositoryRow> findById(Long id) {
    String sql = """
        SELECT id, integration_id, project_id, repo_owner, repo_name, default_branch, status, created_at, updated_at
        FROM git_repositories
        WHERE id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), id).stream().findFirst();
  }

  public Optional<GitRepositoryRow> findByIntegrationAndOwnerAndName(
      Long integrationId,
      String repoOwner,
      String repoName
  ) {
    String sql = """
        SELECT id, integration_id, project_id, repo_owner, repo_name, default_branch, status, created_at, updated_at
        FROM git_repositories
        WHERE integration_id = ? AND repo_owner = ? AND repo_name = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), integrationId, repoOwner, repoName).stream().findFirst();
  }

  public List<GitRepositoryRow> listByIntegration(Long integrationId) {
    String sql = """
        SELECT id, integration_id, project_id, repo_owner, repo_name, default_branch, status, created_at, updated_at
        FROM git_repositories
        WHERE integration_id = ?
        ORDER BY created_at DESC
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), integrationId);
  }

  public boolean exists(Long integrationId, String repoOwner, String repoName) {
    String sql = "SELECT 1 FROM git_repositories WHERE integration_id = ? AND repo_owner = ? AND repo_name = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        integrationId,
        repoOwner,
        repoName
    );
    return value != null;
  }

  private RowMapper<GitRepositoryRow> mapper() {
    return (rs, rowNum) -> new GitRepositoryRow(
        rs.getLong("id"),
        rs.getLong("integration_id"),
        rs.getLong("project_id"),
        rs.getString("repo_owner"),
        rs.getString("repo_name"),
        rs.getString("default_branch"),
        rs.getString("status"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime()
    );
  }

  public record GitRepositoryRow(
      Long id,
      Long integrationId,
      Long projectId,
      String repoOwner,
      String repoName,
      String defaultBranch,
      String status,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
}
