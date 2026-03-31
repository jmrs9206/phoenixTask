package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceGanttRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceGanttRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<ProjectTimelineRow> findProjects() {
    String sql = """
        SELECT id, project_key, name, planned_start_date, planned_end_date
        FROM projects
        ORDER BY project_key
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, projectMapper());
  }

  public Optional<ProjectTimelineRow> findProject(Long projectId) {
    String sql = """
        SELECT id, project_key, name, planned_start_date, planned_end_date
        FROM projects
        WHERE id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, projectMapper(), projectId).stream().findFirst();
  }

  public List<GanttIssueRow> findIssuesByProject(Long projectId) {
    String sql = """
        SELECT i.id,
               i.issue_key,
               i.title,
               i.planned_start_date,
               i.due_date,
               i.status,
               b.baseline_start_date,
               b.baseline_end_date,
               i.assignee_user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS assignee_name
        FROM issues i
        LEFT JOIN gantt_issue_baselines b ON b.issue_id = i.id
        LEFT JOIN users u ON u.id = i.assignee_user_id
        WHERE i.project_id = ?
        ORDER BY i.due_date NULLS LAST, i.planned_start_date NULLS LAST, i.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, issueMapper(), projectId);
  }

  public Optional<ProjectBaselineRow> findProjectBaseline(Long projectId) {
    String sql = """
        SELECT project_id, baseline_start_date, baseline_end_date, captured_at, captured_by_user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS captured_by_name
        FROM gantt_project_baselines b
        LEFT JOIN users u ON u.id = b.captured_by_user_id
        WHERE project_id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, baselineMapper(), projectId).stream().findFirst();
  }

  public List<DependencyRow> findDependenciesByProject(Long projectId) {
    String sql = """
        SELECT d.id,
               d.predecessor_issue_id,
               p.issue_key AS predecessor_key,
               d.successor_issue_id,
               s.issue_key AS successor_key,
               d.dependency_type
        FROM gantt_issue_dependencies d
        JOIN issues p ON p.id = d.predecessor_issue_id
        JOIN issues s ON s.id = d.successor_issue_id
        WHERE p.project_id = ? AND s.project_id = ?
        ORDER BY d.id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, dependencyMapper(), projectId, projectId);
  }

  public Optional<Long> findIssueProjectId(Long issueId) {
    String sql = "SELECT project_id FROM issues WHERE id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, (rs, rowNum) -> rs.getLong("project_id"), issueId)
        .stream()
        .findFirst();
  }

  public DependencyRow insertDependency(Long predecessorIssueId, Long successorIssueId, String dependencyType, Long actorId) {
    String sql = """
        INSERT INTO gantt_issue_dependencies
          (predecessor_issue_id, successor_issue_id, dependency_type, created_at, created_by_user_id)
        VALUES (?, ?, ?, NOW(), ?)
        RETURNING id
        """;
    Long dependencyId = jdbcTemplateProvider.getJdbcTemplate().query(sql, (rs, rowNum) -> rs.getLong("id"),
        predecessorIssueId, successorIssueId, dependencyType, actorId).stream().findFirst().orElseThrow();
    String fetchSql = """
        SELECT d.id,
               d.predecessor_issue_id,
               p.issue_key AS predecessor_key,
               d.successor_issue_id,
               s.issue_key AS successor_key,
               d.dependency_type
        FROM gantt_issue_dependencies d
        JOIN issues p ON p.id = d.predecessor_issue_id
        JOIN issues s ON s.id = d.successor_issue_id
        WHERE d.id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(fetchSql, dependencyMapper(), dependencyId).stream().findFirst().orElseThrow();
  }

  public void upsertProjectBaseline(Long projectId, LocalDate startDate, LocalDate endDate, Long actorId) {
    String sql = """
        INSERT INTO gantt_project_baselines
          (project_id, baseline_start_date, baseline_end_date, captured_at, captured_by_user_id)
        VALUES (?, ?, ?, NOW(), ?)
        ON CONFLICT (project_id)
        DO UPDATE SET baseline_start_date = EXCLUDED.baseline_start_date,
                      baseline_end_date = EXCLUDED.baseline_end_date,
                      captured_at = EXCLUDED.captured_at,
                      captured_by_user_id = EXCLUDED.captured_by_user_id
        """;
    jdbcTemplateProvider.getJdbcTemplate().update(sql, projectId, startDate, endDate, actorId);
  }

  public void upsertIssueBaselines(List<GanttIssueRow> issues, Long actorId) {
    if (issues.isEmpty()) {
      return;
    }
    String sql = """
        INSERT INTO gantt_issue_baselines
          (issue_id, baseline_start_date, baseline_end_date, captured_at, captured_by_user_id)
        VALUES (?, ?, ?, NOW(), ?)
        ON CONFLICT (issue_id)
        DO UPDATE SET baseline_start_date = EXCLUDED.baseline_start_date,
                      baseline_end_date = EXCLUDED.baseline_end_date,
                      captured_at = EXCLUDED.captured_at,
                      captured_by_user_id = EXCLUDED.captured_by_user_id
        """;
    List<Object[]> batch = new ArrayList<>();
    for (GanttIssueRow issue : issues) {
      batch.add(new Object[] { issue.issueId(), issue.plannedStartDate(), issue.dueDate(), actorId });
    }
    jdbcTemplateProvider.getJdbcTemplate().batchUpdate(sql, batch);
  }

  private RowMapper<ProjectTimelineRow> projectMapper() {
    return (rs, rowNum) -> new ProjectTimelineRow(
        rs.getLong("id"),
        rs.getString("project_key"),
        rs.getString("name"),
        rs.getDate("planned_start_date") != null ? rs.getDate("planned_start_date").toLocalDate() : null,
        rs.getDate("planned_end_date") != null ? rs.getDate("planned_end_date").toLocalDate() : null
    );
  }

  private RowMapper<GanttIssueRow> issueMapper() {
    return (rs, rowNum) -> new GanttIssueRow(
        rs.getLong("id"),
        rs.getString("issue_key"),
        rs.getString("title"),
        rs.getDate("planned_start_date") != null ? rs.getDate("planned_start_date").toLocalDate() : null,
        rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null,
        rs.getString("status"),
        rs.getDate("baseline_start_date") != null ? rs.getDate("baseline_start_date").toLocalDate() : null,
        rs.getDate("baseline_end_date") != null ? rs.getDate("baseline_end_date").toLocalDate() : null,
        (Long) rs.getObject("assignee_user_id"),
        rs.getString("assignee_name")
    );
  }

  private RowMapper<ProjectBaselineRow> baselineMapper() {
    return (rs, rowNum) -> new ProjectBaselineRow(
        rs.getLong("project_id"),
        rs.getDate("baseline_start_date") != null ? rs.getDate("baseline_start_date").toLocalDate() : null,
        rs.getDate("baseline_end_date") != null ? rs.getDate("baseline_end_date").toLocalDate() : null,
        rs.getTimestamp("captured_at") != null ? rs.getTimestamp("captured_at").toLocalDateTime() : null,
        (Long) rs.getObject("captured_by_user_id"),
        rs.getString("captured_by_name")
    );
  }

  private RowMapper<DependencyRow> dependencyMapper() {
    return (rs, rowNum) -> new DependencyRow(
        rs.getLong("id"),
        rs.getLong("predecessor_issue_id"),
        rs.getString("predecessor_key"),
        rs.getLong("successor_issue_id"),
        rs.getString("successor_key"),
        rs.getString("dependency_type")
    );
  }

  public record ProjectTimelineRow(
      Long projectId,
      String projectKey,
      String projectName,
      LocalDate plannedStartDate,
      LocalDate plannedEndDate
  ) {}

  public record GanttIssueRow(
      Long issueId,
      String issueKey,
      String title,
      LocalDate plannedStartDate,
      LocalDate dueDate,
      String status,
      LocalDate baselineStartDate,
      LocalDate baselineEndDate,
      Long assigneeUserId,
      String assigneeName
  ) {}

  public record ProjectBaselineRow(
      Long projectId,
      LocalDate baselineStartDate,
      LocalDate baselineEndDate,
      LocalDateTime capturedAt,
      Long capturedByUserId,
      String capturedByName
  ) {}

  public record DependencyRow(
      Long dependencyId,
      Long predecessorIssueId,
      String predecessorIssueKey,
      Long successorIssueId,
      String successorIssueKey,
      String dependencyType
  ) {}
}
