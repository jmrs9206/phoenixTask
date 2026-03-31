package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceKanbanRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceKanbanRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<ProjectSummaryRow> findProjectSummaries() {
    String sql = """
        SELECT p.id,
               p.project_key,
               p.name,
               COALESCE(SUM(CASE WHEN i.status = 'OPEN' THEN 1 ELSE 0 END), 0) AS open_count,
               COALESCE(SUM(CASE WHEN i.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) AS in_progress_count,
               COALESCE(SUM(CASE WHEN i.status = 'BLOCKED' THEN 1 ELSE 0 END), 0) AS blocked_count,
               COALESCE(SUM(CASE WHEN i.status = 'DONE' THEN 1 ELSE 0 END), 0) AS done_count
        FROM projects p
        LEFT JOIN issues i ON i.project_id = p.id
        GROUP BY p.id, p.project_key, p.name
        ORDER BY p.id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, summaryMapper());
  }

  public List<KanbanIssueRow> findIssuesByProject(Long projectId) {
    String sql = """
        SELECT i.id,
               i.issue_key,
               i.title,
               i.status,
               i.priority,
               i.updated_at,
               a.id AS assignee_id,
               COALESCE(CONCAT(a.first_name, ' ', a.last_name), 'Unassigned') AS assignee_name
        FROM issues i
        LEFT JOIN users a ON a.id = i.assignee_user_id
        WHERE i.project_id = ?
        ORDER BY i.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, issueMapper(), projectId);
  }

  public List<ColumnPolicyRow> findColumnPoliciesByProject(Long projectId) {
    String sql = """
        SELECT status,
               wip_limit,
               policy_text
        FROM kanban_column_policies
        WHERE project_id = ?
        ORDER BY status
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, policyMapper(), projectId);
  }

  private RowMapper<ProjectSummaryRow> summaryMapper() {
    return (rs, rowNum) -> new ProjectSummaryRow(
        rs.getLong("id"),
        rs.getString("project_key"),
        rs.getString("name"),
        rs.getInt("open_count"),
        rs.getInt("in_progress_count"),
        rs.getInt("blocked_count"),
        rs.getInt("done_count")
    );
  }

  private RowMapper<KanbanIssueRow> issueMapper() {
    return (rs, rowNum) -> new KanbanIssueRow(
        rs.getLong("id"),
        rs.getString("issue_key"),
        rs.getString("title"),
        rs.getString("status"),
        rs.getString("priority"),
        rs.getObject("updated_at", java.time.LocalDateTime.class),
        rs.getObject("assignee_id", Long.class),
        rs.getString("assignee_name")
    );
  }

  private RowMapper<ColumnPolicyRow> policyMapper() {
    return (rs, rowNum) -> new ColumnPolicyRow(
        rs.getString("status"),
        rs.getObject("wip_limit", Integer.class),
        rs.getString("policy_text")
    );
  }

  public record ProjectSummaryRow(
      Long projectId,
      String projectKey,
      String projectName,
      int openCount,
      int inProgressCount,
      int blockedCount,
      int doneCount
  ) {}

  public record KanbanIssueRow(
      Long issueId,
      String issueKey,
      String title,
      String status,
      String priority,
      java.time.LocalDateTime updatedAt,
      Long assigneeId,
      String assigneeName
  ) {}

  public record ColumnPolicyRow(
      String status,
      Integer wipLimit,
      String policyText
  ) {}
}
