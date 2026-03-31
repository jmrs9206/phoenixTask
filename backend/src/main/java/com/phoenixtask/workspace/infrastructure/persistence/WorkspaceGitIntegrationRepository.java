package com.phoenixtask.workspace.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceGitIntegrationRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceGitIntegrationRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public GitIntegrationRow insert(
      Long companyId,
      String provider,
      String label,
      String tokenHash,
      String tokenPrefix,
      String webhookSecret,
      String status
  ) {
    String sql = """
        INSERT INTO git_integrations
          (company_id, provider, label, token_hash, token_prefix, webhook_secret, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        RETURNING id, company_id, provider, label, token_hash, token_prefix, webhook_secret, status, created_at, updated_at, revoked_at
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), companyId, provider, label, tokenHash, tokenPrefix, webhookSecret, status)
        .stream()
        .findFirst()
        .orElse(null);
  }

  public Optional<GitIntegrationRow> findById(Long id) {
    String sql = """
        SELECT id, company_id, provider, label, token_hash, token_prefix, webhook_secret,
               status, created_at, updated_at, revoked_at
        FROM git_integrations
        WHERE id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), id).stream().findFirst();
  }

  public List<GitIntegrationRow> listByCompany(Long companyId) {
    String sql = """
        SELECT id, company_id, provider, label, token_hash, token_prefix, webhook_secret,
               status, created_at, updated_at, revoked_at
        FROM git_integrations
        WHERE company_id = ?
        ORDER BY created_at DESC
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), companyId);
  }

  public Optional<GitIntegrationRow> revoke(Long id, Long companyId) {
    String sql = """
        UPDATE git_integrations
        SET status = 'REVOKED', revoked_at = NOW(), updated_at = NOW()
        WHERE id = ? AND company_id = ?
        RETURNING id, company_id, provider, label, token_hash, token_prefix, webhook_secret,
                  status, created_at, updated_at, revoked_at
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), id, companyId).stream().findFirst();
  }

  private RowMapper<GitIntegrationRow> mapper() {
    return (rs, rowNum) -> new GitIntegrationRow(
        rs.getLong("id"),
        rs.getLong("company_id"),
        rs.getString("provider"),
        rs.getString("label"),
        rs.getString("token_hash"),
        rs.getString("token_prefix"),
        rs.getString("webhook_secret"),
        rs.getString("status"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime(),
        rs.getTimestamp("revoked_at") != null ? rs.getTimestamp("revoked_at").toLocalDateTime() : null
    );
  }

  public record GitIntegrationRow(
      Long id,
      Long companyId,
      String provider,
      String label,
      String tokenHash,
      String tokenPrefix,
      String webhookSecret,
      String status,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      LocalDateTime revokedAt
  ) {}
}
