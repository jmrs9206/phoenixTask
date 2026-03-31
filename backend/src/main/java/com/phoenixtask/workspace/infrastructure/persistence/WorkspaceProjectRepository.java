package com.phoenixtask.workspace.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceProjectRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceProjectRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<ProjectRow> findAll() {
    String sql = """
        SELECT id, company_id, project_key, name, description, status, created_at, updated_at
        FROM projects
        ORDER BY id
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper());
  }

  public List<ProjectRow> findAllPaged(String status, String query, int limit, int offset) {
    StringBuilder sql = new StringBuilder("""
        SELECT id, company_id, project_key, name, description, status, created_at, updated_at
        FROM projects
        WHERE 1=1
        """);
    java.util.List<Object> params = new java.util.ArrayList<>();
    String normalizedStatus = normalize(status);
    if (normalizedStatus != null) {
      sql.append(" AND UPPER(status) = ?");
      params.add(normalizedStatus);
    }
    String search = normalizeQuery(query);
    if (search != null) {
      sql.append(" AND (name ILIKE ? OR project_key ILIKE ?)");
      String like = "%" + search + "%";
      params.add(like);
      params.add(like);
    }
    sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql.toString(), mapper(), params.toArray());
  }

  public long countAll(String status, String query) {
    StringBuilder sql = new StringBuilder("""
        SELECT COUNT(*)
        FROM projects
        WHERE 1=1
        """);
    java.util.List<Object> params = new java.util.ArrayList<>();
    String normalizedStatus = normalize(status);
    if (normalizedStatus != null) {
      sql.append(" AND UPPER(status) = ?");
      params.add(normalizedStatus);
    }
    String search = normalizeQuery(query);
    if (search != null) {
      sql.append(" AND (name ILIKE ? OR project_key ILIKE ?)");
      String like = "%" + search + "%";
      params.add(like);
      params.add(like);
    }
    Long count = jdbcTemplateProvider.getJdbcTemplate().query(
        sql.toString(),
        rs -> rs.next() ? rs.getLong(1) : 0L,
        params.toArray()
    );
    return count == null ? 0L : count;
  }

  public java.util.Optional<ProjectRow> findById(Long projectId) {
    String sql = """
        SELECT id, company_id, project_key, name, description, status, created_at, updated_at
        FROM projects
        WHERE id = ?
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper(), projectId).stream().findFirst();
  }

  public boolean existsByProjectKey(String projectKey) {
    String sql = "SELECT 1 FROM projects WHERE project_key = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        projectKey
    );
    return value != null;
  }

  public boolean existsById(Long projectId) {
    String sql = "SELECT 1 FROM projects WHERE id = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        projectId
    );
    return value != null;
  }

  public Long getCompanyId() {
    String sql = "SELECT id FROM company LIMIT 1";
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null
    );
  }

  public Long insertProject(Long companyId, String projectKey, String name, String description) {
    String sql = """
        INSERT INTO projects (company_id, project_key, name, description, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, 'ACTIVE', NOW(), NOW())
        RETURNING id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        companyId,
        projectKey,
        name,
        description
    );
  }

  public java.time.LocalDateTime fetchCreatedAt(Long projectId) {
    String sql = "SELECT created_at FROM projects WHERE id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getTimestamp("created_at").toLocalDateTime() : null,
        projectId
    );
  }

  public java.util.Optional<ProjectKeyRow> findProjectKey(Long projectId) {
    String sql = "SELECT id, project_key FROM projects WHERE id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, keyMapper(), projectId).stream().findFirst();
  }

  public java.util.Optional<ProjectSummaryRow> findSummaryById(Long projectId) {
    String sql = "SELECT id, project_key, name FROM projects WHERE id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, summaryMapper(), projectId).stream().findFirst();
  }

  private RowMapper<ProjectKeyRow> keyMapper() {
    return (rs, rowNum) -> new ProjectKeyRow(
        rs.getLong("id"),
        rs.getString("project_key")
    );
  }

  private RowMapper<ProjectRow> mapper() {
    return (rs, rowNum) -> new ProjectRow(
        rs.getLong("id"),
        rs.getLong("company_id"),
        rs.getString("project_key"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getString("status"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime()
    );
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

  public record ProjectRow(
      Long id,
      Long companyId,
      String projectKey,
      String name,
      String description,
      String status,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}

  public record ProjectKeyRow(
      Long id,
      String projectKey
  ) {}

  private RowMapper<ProjectSummaryRow> summaryMapper() {
    return (rs, rowNum) -> new ProjectSummaryRow(
        rs.getLong("id"),
        rs.getString("project_key"),
        rs.getString("name")
    );
  }

  public record ProjectSummaryRow(
      Long id,
      String projectKey,
      String name
  ) {}
}
