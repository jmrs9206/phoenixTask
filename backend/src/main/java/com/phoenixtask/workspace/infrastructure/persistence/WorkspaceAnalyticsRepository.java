package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceAnalyticsRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceAnalyticsRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<CountByStatusRow> countIssuesByStatus() {
    String sql = "SELECT status, COUNT(*) AS count FROM issues GROUP BY status";
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, statusMapper());
  }

  public List<CountByStatusRow> countIssuesByPriority() {
    String sql = "SELECT priority AS status, COUNT(*) AS count FROM issues GROUP BY priority";
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, statusMapper());
  }

  public List<IssuesByProjectRow> countIssuesByProject() {
    String sql = """
        SELECT p.id AS project_id, p.project_key, COUNT(i.id) AS count
        FROM projects p
        LEFT JOIN issues i ON i.project_id = p.id
        GROUP BY p.id, p.project_key
        ORDER BY p.id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, projectMapper());
  }

  public List<SprintBacklogRow> sprintBacklogCounts() {
    String sql = """
        SELECT p.id AS project_id,
               p.project_key,
               COALESCE(SUM(CASE WHEN i.sprint_id IS NULL THEN 1 ELSE 0 END), 0) AS backlog,
               COALESCE(SUM(CASE WHEN s.status = 'ACTIVE' THEN 1 ELSE 0 END), 0) AS active_sprint
        FROM projects p
        LEFT JOIN issues i ON i.project_id = p.id
        LEFT JOIN sprints s ON s.id = i.sprint_id
        GROUP BY p.id, p.project_key
        ORDER BY p.id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, sprintMapper());
  }

  public long countTeams() {
    return count("teams");
  }

  public long countProjects() {
    return count("projects");
  }

  public long countUsers() {
    return count("users");
  }

  public long countMessages() {
    return count("messages");
  }

  public List<CfdRow> cfdSeries(int windowDays) {
    String sql = """
        WITH days AS (
          SELECT (CURRENT_DATE - (? - 1) * INTERVAL '1 day' + (g * INTERVAL '1 day'))::date AS day
          FROM generate_series(0, ? - 1) AS g
        )
        SELECT d.day,
               COALESCE(SUM(CASE WHEN i.status = 'OPEN' AND i.created_at::date <= d.day THEN 1 ELSE 0 END), 0) AS open_count,
               COALESCE(SUM(CASE WHEN i.status = 'IN_PROGRESS' AND i.created_at::date <= d.day THEN 1 ELSE 0 END), 0) AS in_progress_count,
               COALESCE(SUM(CASE WHEN i.status = 'BLOCKED' AND i.created_at::date <= d.day THEN 1 ELSE 0 END), 0) AS blocked_count,
               COALESCE(SUM(CASE WHEN i.status = 'DONE' AND i.created_at::date <= d.day AND i.updated_at::date <= d.day THEN 1 ELSE 0 END), 0) AS done_count
        FROM days d
        LEFT JOIN issues i ON i.created_at::date <= d.day
        GROUP BY d.day
        ORDER BY d.day
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, cfdMapper(), windowDays, windowDays);
  }

  public List<CycleTimeRow> completedCycleTimes(int limit) {
    String sql = """
        SELECT issue_key,
               title,
               updated_at::date AS completed_on,
               GREATEST(1, DATE_PART('day', updated_at - created_at))::numeric AS cycle_time_days
        FROM issues
        WHERE status = 'DONE'
        ORDER BY updated_at DESC
        LIMIT ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, cycleTimeMapper(), limit);
  }

  public TechDebtRow techDebtCounts() {
    String sql = """
        SELECT COALESCE(SUM(CASE WHEN is_tech_debt AND status != 'DONE' THEN 1 ELSE 0 END), 0) AS debt_count,
               COALESCE(SUM(CASE WHEN status != 'DONE' THEN 1 ELSE 0 END), 0) AS total_open
        FROM issues
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? new TechDebtRow(rs.getLong("debt_count"), rs.getLong("total_open")) : new TechDebtRow(0, 0)
    );
  }

  private long count(String table) {
    String sql = "SELECT COUNT(*) AS count FROM " + table;
    Long value = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("count") : 0L
    );
    return value == null ? 0L : value;
  }

  private RowMapper<CountByStatusRow> statusMapper() {
    return (rs, rowNum) -> new CountByStatusRow(
        rs.getString("status"),
        rs.getLong("count")
    );
  }

  private RowMapper<IssuesByProjectRow> projectMapper() {
    return (rs, rowNum) -> new IssuesByProjectRow(
        rs.getLong("project_id"),
        rs.getString("project_key"),
        rs.getLong("count")
    );
  }

  private RowMapper<CfdRow> cfdMapper() {
    return (rs, rowNum) -> new CfdRow(
        rs.getDate("day").toLocalDate(),
        rs.getInt("open_count"),
        rs.getInt("in_progress_count"),
        rs.getInt("blocked_count"),
        rs.getInt("done_count")
    );
  }

  private RowMapper<CycleTimeRow> cycleTimeMapper() {
    return (rs, rowNum) -> new CycleTimeRow(
        rs.getString("issue_key"),
        rs.getString("title"),
        rs.getDate("completed_on").toLocalDate(),
        rs.getBigDecimal("cycle_time_days")
    );
  }

  private RowMapper<SprintBacklogRow> sprintMapper() {
    return (rs, rowNum) -> new SprintBacklogRow(
        rs.getLong("project_id"),
        rs.getString("project_key"),
        rs.getLong("backlog"),
        rs.getLong("active_sprint")
    );
  }

  public record CountByStatusRow(String status, long count) {}

  public record IssuesByProjectRow(Long projectId, String projectKey, long count) {}

  public record SprintBacklogRow(Long projectId, String projectKey, long backlog, long activeSprint) {}

  public record CfdRow(java.time.LocalDate day, int open, int inProgress, int blocked, int done) {}

  public record CycleTimeRow(String issueKey, String title, java.time.LocalDate completedOn, java.math.BigDecimal cycleTimeDays) {}

  public record TechDebtRow(long debtCount, long totalOpen) {}
}
