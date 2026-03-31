package com.phoenixtask.workspace.infrastructure.persistence;

import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceGitWebhookEventRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceGitWebhookEventRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public void insert(
      Long integrationId,
      String provider,
      String eventType,
      String deliveryId,
      String repoFullName,
      boolean signatureValid
  ) {
    String sql = """
        INSERT INTO git_webhook_events
          (integration_id, provider, event_type, delivery_id, repo_full_name, signature_valid, created_at)
        VALUES (?, ?, ?, ?, ?, ?, NOW())
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    jdbcTemplate.update(sql, integrationId, provider, eventType, deliveryId, repoFullName, signatureValid);
  }

  public record GitWebhookEventRow(
      Long integrationId,
      String provider,
      String eventType,
      String deliveryId,
      String repoFullName,
      boolean signatureValid,
      LocalDateTime createdAt
  ) {}
}
