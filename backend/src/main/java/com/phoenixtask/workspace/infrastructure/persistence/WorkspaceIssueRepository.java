package com.phoenixtask.workspace.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceIssueRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceIssueRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<IssueListRow> findAll() {
    String sql = """
        SELECT i.id,
               i.issue_key,
               i.title,
               p.project_key,
               i.status,
               i.priority,
               COALESCE(CONCAT(a.first_name, ' ', a.last_name), 'Unassigned') AS assignee_name,
               i.created_at
        FROM issues i
        JOIN projects p ON p.id = i.project_id
        LEFT JOIN users a ON a.id = i.assignee_user_id
        ORDER BY i.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, listMapper());
  }

  public List<IssueListRow> findAllForUser(Long userId) {
    String sql = """
        SELECT DISTINCT i.id,
               i.issue_key,
               i.title,
               p.project_key,
               i.status,
               i.priority,
               COALESCE(CONCAT(a.first_name, ' ', a.last_name), 'Unassigned') AS assignee_name,
               i.created_at
        FROM issues i
        JOIN projects p ON p.id = i.project_id
        JOIN project_memberships pm ON pm.project_id = i.project_id
        LEFT JOIN users a ON a.id = i.assignee_user_id
        WHERE pm.user_id = ?
        ORDER BY i.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, listMapper(), userId);
  }

  public List<IssueListRow> findAllForUserPaged(
      Long userId,
      Long projectId,
      String status,
      String priority,
      Long assigneeId,
      String query,
      int limit,
      int offset
  ) {
    StringBuilder sql = new StringBuilder("""
        SELECT DISTINCT i.id,
               i.issue_key,
               i.title,
               p.project_key,
               i.status,
               i.priority,
               COALESCE(CONCAT(a.first_name, ' ', a.last_name), 'Unassigned') AS assignee_name,
               i.created_at
        FROM issues i
        JOIN projects p ON p.id = i.project_id
        JOIN project_memberships pm ON pm.project_id = i.project_id
        LEFT JOIN users a ON a.id = i.assignee_user_id
        WHERE pm.user_id = ?
        """);
    java.util.List<Object> params = new java.util.ArrayList<>();
    params.add(userId);
    applyIssueFilters(sql, params, projectId, status, priority, assigneeId, query);
    sql.append(" ORDER BY i.created_at DESC LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);
    return jdbcTemplateProvider.getJdbcTemplate().query(sql.toString(), listMapper(), params.toArray());
  }

  public long countAllForUser(
      Long userId,
      Long projectId,
      String status,
      String priority,
      Long assigneeId,
      String query
  ) {
    StringBuilder sql = new StringBuilder("""
        SELECT COUNT(DISTINCT i.id)
        FROM issues i
        JOIN project_memberships pm ON pm.project_id = i.project_id
        WHERE pm.user_id = ?
        """);
    java.util.List<Object> params = new java.util.ArrayList<>();
    params.add(userId);
    applyIssueFilters(sql, params, projectId, status, priority, assigneeId, query);
    Long count = jdbcTemplateProvider.getJdbcTemplate().query(
        sql.toString(),
        rs -> rs.next() ? rs.getLong(1) : 0L,
        params.toArray()
    );
    return count == null ? 0L : count;
  }

  public List<IssueListRow> findAllPaged(
      Long projectId,
      String status,
      String priority,
      Long assigneeId,
      String query,
      int limit,
      int offset
  ) {
    StringBuilder sql = new StringBuilder("""
        SELECT i.id,
               i.issue_key,
               i.title,
               p.project_key,
               i.status,
               i.priority,
               COALESCE(CONCAT(a.first_name, ' ', a.last_name), 'Unassigned') AS assignee_name,
               i.created_at
        FROM issues i
        JOIN projects p ON p.id = i.project_id
        LEFT JOIN users a ON a.id = i.assignee_user_id
        WHERE 1=1
        """);
    java.util.List<Object> params = new java.util.ArrayList<>();
    applyIssueFilters(sql, params, projectId, status, priority, assigneeId, query);
    sql.append(" ORDER BY i.created_at DESC LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);
    return jdbcTemplateProvider.getJdbcTemplate().query(sql.toString(), listMapper(), params.toArray());
  }

  public long countAll(
      Long projectId,
      String status,
      String priority,
      Long assigneeId,
      String query
  ) {
    StringBuilder sql = new StringBuilder("""
        SELECT COUNT(*)
        FROM issues i
        WHERE 1=1
        """);
    java.util.List<Object> params = new java.util.ArrayList<>();
    applyIssueFilters(sql, params, projectId, status, priority, assigneeId, query);
    Long count = jdbcTemplateProvider.getJdbcTemplate().query(
        sql.toString(),
        rs -> rs.next() ? rs.getLong(1) : 0L,
        params.toArray()
    );
    return count == null ? 0L : count;
  }

  public List<IssueSummaryRow> findByProject(Long projectId) {
    String sql = """
        SELECT i.id,
               i.issue_key,
               i.title,
               i.status,
               i.priority,
               COALESCE(CONCAT(a.first_name, ' ', a.last_name), 'Unassigned') AS assignee_name
        FROM issues i
        LEFT JOIN users a ON a.id = i.assignee_user_id
        WHERE i.project_id = ?
        ORDER BY i.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, summaryMapper(), projectId);
  }

  private void applyIssueFilters(
      StringBuilder sql,
      java.util.List<Object> params,
      Long projectId,
      String status,
      String priority,
      Long assigneeId,
      String query
  ) {
    if (projectId != null && projectId > 0) {
      sql.append(" AND i.project_id = ?");
      params.add(projectId);
    }
    String normalizedStatus = normalize(status);
    if (normalizedStatus != null) {
      sql.append(" AND UPPER(i.status) = ?");
      params.add(normalizedStatus);
    }
    String normalizedPriority = normalize(priority);
    if (normalizedPriority != null) {
      sql.append(" AND UPPER(i.priority) = ?");
      params.add(normalizedPriority);
    }
    if (assigneeId != null && assigneeId > 0) {
      sql.append(" AND i.assignee_user_id = ?");
      params.add(assigneeId);
    }
    String search = normalizeQuery(query);
    if (search != null) {
      sql.append(" AND (i.title ILIKE ? OR i.issue_key ILIKE ?)");
      String like = "%" + search + "%";
      params.add(like);
      params.add(like);
    }
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.isBlank()) {
      return null;
    }
    return trimmed.toUpperCase();
  }

  private String normalizeQuery(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  public Optional<IssueDetailRow> findById(Long issueId) {
    String sql = """
        SELECT i.id,
               i.issue_key,
               i.title,
               i.description,
               p.project_key,
               i.status,
               i.priority,
               CONCAT(r.first_name, ' ', r.last_name) AS reporter_name,
               COALESCE(CONCAT(a.first_name, ' ', a.last_name), 'Unassigned') AS assignee_name,
               i.due_date,
               i.created_at
        FROM issues i
        JOIN projects p ON p.id = i.project_id
        JOIN users r ON r.id = i.reporter_user_id
        LEFT JOIN users a ON a.id = i.assignee_user_id
        WHERE i.id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, detailMapper(), issueId).stream().findFirst();
  }

  public Optional<IssueLookupRow> findByIssueKey(String issueKey) {
    String sql = """
        SELECT id, project_id
        FROM issues
        WHERE issue_key = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate()
        .query(sql, issueLookupMapper(), issueKey)
        .stream()
        .findFirst();
  }

  public boolean existsById(Long issueId) {
    String sql = "SELECT 1 FROM issues WHERE id = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        issueId
    );
    return value != null;
  }

  public Long findProjectId(Long issueId) {
    String sql = "SELECT project_id FROM issues WHERE id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("project_id") : null,
        issueId
    );
  }

  public List<IssueSummaryRow> findBacklogByProject(Long projectId) {
    String sql = """
        SELECT i.id,
               i.issue_key,
               i.title,
               i.status,
               i.priority,
               COALESCE(CONCAT(a.first_name, ' ', a.last_name), 'Unassigned') AS assignee_name
        FROM issues i
        LEFT JOIN users a ON a.id = i.assignee_user_id
        WHERE i.project_id = ?
          AND i.sprint_id IS NULL
        ORDER BY i.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, summaryMapper(), projectId);
  }

  public List<IssueSummaryRow> findBySprint(Long sprintId) {
    String sql = """
        SELECT i.id,
               i.issue_key,
               i.title,
               i.status,
               i.priority,
               COALESCE(CONCAT(a.first_name, ' ', a.last_name), 'Unassigned') AS assignee_name
        FROM issues i
        LEFT JOIN users a ON a.id = i.assignee_user_id
        WHERE i.sprint_id = ?
        ORDER BY i.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, summaryMapper(), sprintId);
  }

  public List<Long> findIssueIdsBySprint(Long sprintId) {
    String sql = "SELECT id FROM issues WHERE sprint_id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        (rs, rowNum) -> rs.getLong("id"),
        sprintId
    );
  }

  public IssueKeyRow incrementIssueCounter(Long projectId) {
    String sql = """
        UPDATE projects
        SET issue_counter = issue_counter + 1
        WHERE id = ?
        RETURNING issue_counter
        """;
    Long counter = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("issue_counter") : null,
        projectId
    );
    return counter == null ? null : new IssueKeyRow(counter);
  }

  public Long insertIssue(
      Long projectId,
      String issueKey,
      String title,
      String description,
      String status,
      String priority,
      Long reporterUserId,
      Long assigneeUserId
  ) {
    String sql = """
        INSERT INTO issues
          (project_id, issue_key, title, description, status, priority, reporter_user_id, assignee_user_id, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        RETURNING id
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        projectId,
        issueKey,
        title,
        description,
        status,
        priority,
        reporterUserId,
        assigneeUserId
    );
  }

  public Long findCompanyIdForUser(Long userId) {
    String sql = "SELECT company_id FROM users WHERE id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("company_id") : null,
        userId
    );
  }

  public boolean isUserInProject(Long projectId, Long userId) {
    String sql = "SELECT 1 FROM project_memberships WHERE project_id = ? AND user_id = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        projectId,
        userId
    );
    return value != null;
  }

  public void assignIssueToSprint(Long issueId, Long sprintId) {
    String sql = "UPDATE issues SET sprint_id = ? WHERE id = ?";
    jdbcTemplateProvider.getJdbcTemplate().update(sql, sprintId, issueId);
  }

  public void moveIssueToBacklog(Long issueId) {
    String sql = "UPDATE issues SET sprint_id = NULL WHERE id = ?";
    jdbcTemplateProvider.getJdbcTemplate().update(sql, issueId);
  }

  private RowMapper<IssueListRow> listMapper() {
    return (rs, rowNum) -> new IssueListRow(
        rs.getLong("id"),
        rs.getString("issue_key"),
        rs.getString("title"),
        rs.getString("project_key"),
        rs.getString("status"),
        rs.getString("priority"),
        rs.getString("assignee_name"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }

  private RowMapper<IssueDetailRow> detailMapper() {
    return (rs, rowNum) -> new IssueDetailRow(
        rs.getLong("id"),
        rs.getString("issue_key"),
        rs.getString("title"),
        rs.getString("description"),
        rs.getString("project_key"),
        rs.getString("status"),
        rs.getString("priority"),
        rs.getString("reporter_name"),
        rs.getString("assignee_name"),
        rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null,
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }

  private RowMapper<IssueLookupRow> issueLookupMapper() {
    return (rs, rowNum) -> new IssueLookupRow(
        rs.getLong("id"),
        rs.getLong("project_id")
    );
  }

  private RowMapper<IssueSummaryRow> summaryMapper() {
    return (rs, rowNum) -> new IssueSummaryRow(
        rs.getLong("id"),
        rs.getString("issue_key"),
        rs.getString("title"),
        rs.getString("status"),
        rs.getString("priority"),
        rs.getString("assignee_name")
    );
  }

  public record IssueListRow(
      Long id,
      String issueKey,
      String title,
      String projectKey,
      String status,
      String priority,
      String assigneeName,
      LocalDateTime createdAt
  ) {}

  public record IssueDetailRow(
      Long id,
      String issueKey,
      String title,
      String description,
      String projectKey,
      String status,
      String priority,
      String reporterName,
      String assigneeName,
      LocalDate dueDate,
      LocalDateTime createdAt
  ) {}

  public record IssueSummaryRow(
      Long id,
      String issueKey,
      String title,
      String status,
      String priority,
      String assigneeName
  ) {}

  public record IssueLookupRow(
      Long id,
      Long projectId
  ) {}

  public record IssueKeyRow(Long counter) {}
}
