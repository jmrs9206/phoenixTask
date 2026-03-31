package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceSprintRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceSprintRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<SprintRow> findByProject(Long projectId) {
    String sql = """
        SELECT id, project_id, name, goal, status, start_date, end_date, created_at, updated_at
        FROM sprints
        WHERE project_id = ?
        ORDER BY start_date
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), projectId);
  }

  public Optional<SprintRow> findById(Long sprintId) {
    String sql = """
        SELECT id, project_id, name, goal, status, start_date, end_date, created_at, updated_at
        FROM sprints
        WHERE id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, mapper(), sprintId).stream().findFirst();
  }

  public boolean hasActiveSprint(Long projectId) {
    String sql = "SELECT 1 FROM sprints WHERE project_id = ? AND status = 'ACTIVE'";
    Integer value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? 1 : null,
        projectId
    );
    return value != null;
  }

  public Long insertSprint(
      Long projectId,
      String name,
      String goal,
      String status,
      LocalDate startDate,
      LocalDate endDate
  ) {
    String sql = """
        INSERT INTO sprints (project_id, name, goal, status, start_date, end_date, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
        RETURNING id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        projectId,
        name,
        goal,
        status,
        startDate,
        endDate
    );
  }

  public void updateSprint(Long sprintId, String goal, String status) {
    String sql = """
        UPDATE sprints
        SET goal = ?, status = ?, updated_at = NOW()
        WHERE id = ?
        """;
    jdbcTemplateProvider.getJdbcTemplate().update(sql, goal, status, sprintId);
  }

  private RowMapper<SprintRow> mapper() {
    return (rs, rowNum) -> new SprintRow(
        rs.getLong("id"),
        rs.getLong("project_id"),
        rs.getString("name"),
        rs.getString("goal"),
        rs.getString("status"),
        rs.getDate("start_date").toLocalDate(),
        rs.getDate("end_date").toLocalDate(),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime()
    );
  }

  public record SprintRow(
      Long id,
      Long projectId,
      String name,
      String goal,
      String status,
      LocalDate startDate,
      LocalDate endDate,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
}
