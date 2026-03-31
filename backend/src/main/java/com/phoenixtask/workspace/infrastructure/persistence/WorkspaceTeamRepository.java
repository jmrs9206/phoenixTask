package com.phoenixtask.workspace.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceTeamRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceTeamRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<TeamRow> findAll() {
    String sql = """
        SELECT id, company_id, name, description, created_at, updated_at
        FROM teams
        ORDER BY id
        """;
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper());
  }

  public List<TeamRow> findAllPaged(String query, int limit, int offset) {
    StringBuilder sql = new StringBuilder("""
        SELECT id, company_id, name, description, created_at, updated_at
        FROM teams
        WHERE 1=1
        """);
    java.util.List<Object> params = new java.util.ArrayList<>();
    String search = normalizeQuery(query);
    if (search != null) {
      sql.append(" AND name ILIKE ?");
      params.add("%" + search + "%");
    }
    sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);
    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql.toString(), mapper(), params.toArray());
  }

  public long countAll(String query) {
    StringBuilder sql = new StringBuilder("""
        SELECT COUNT(*)
        FROM teams
        WHERE 1=1
        """);
    java.util.List<Object> params = new java.util.ArrayList<>();
    String search = normalizeQuery(query);
    if (search != null) {
      sql.append(" AND name ILIKE ?");
      params.add("%" + search + "%");
    }
    Long count = jdbcTemplateProvider.getJdbcTemplate().query(
        sql.toString(),
        rs -> rs.next() ? rs.getLong(1) : 0L,
        params.toArray()
    );
    return count == null ? 0L : count;
  }

  public boolean existsByName(String name) {
    String sql = "SELECT 1 FROM teams WHERE name = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        name
    );
    return value != null;
  }

  public boolean existsById(Long teamId) {
    String sql = "SELECT 1 FROM teams WHERE id = ?";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        teamId
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

  public Long insertTeam(Long companyId, String name, String description) {
    String sql = """
        INSERT INTO teams (company_id, name, description, created_at, updated_at)
        VALUES (?, ?, ?, NOW(), NOW())
        RETURNING id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        companyId,
        name,
        description
    );
  }

  public java.time.LocalDateTime fetchCreatedAt(Long teamId) {
    String sql = "SELECT created_at FROM teams WHERE id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getTimestamp("created_at").toLocalDateTime() : null,
        teamId
    );
  }


  private RowMapper<TeamRow> mapper() {
    return (rs, rowNum) -> new TeamRow(
        rs.getLong("id"),
        rs.getLong("company_id"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime()
    );
  }

  private String normalizeQuery(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  public record TeamRow(
      Long id,
      Long companyId,
      String name,
      String description,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}

}
