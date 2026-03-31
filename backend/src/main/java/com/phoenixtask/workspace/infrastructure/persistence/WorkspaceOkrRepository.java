package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceOkrRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceOkrRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public List<ObjectiveRow> findObjectives(Long projectId) {
    String sql = """
        SELECT o.id,
               o.company_id,
               o.project_id,
               p.project_key,
               p.name AS project_name,
               o.owner_user_id,
               o.title,
               o.description,
               o.status,
               o.period_start,
               o.period_end,
               o.confidence_level,
               o.final_score,
               o.closed_at,
               CONCAT(u.first_name, ' ', u.last_name) AS owner_name
        FROM okr_objectives o
        JOIN projects p ON p.id = o.project_id
        JOIN users u ON u.id = o.owner_user_id
        WHERE o.project_id = ?
        ORDER BY o.period_start DESC, o.id DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, objectiveMapper(), projectId);
  }

  public Optional<ObjectiveRow> findObjectiveById(Long objectiveId) {
    String sql = """
        SELECT o.id,
               o.company_id,
               o.project_id,
               p.project_key,
               p.name AS project_name,
               o.owner_user_id,
               o.title,
               o.description,
               o.status,
               o.period_start,
               o.period_end,
               o.confidence_level,
               o.final_score,
               o.closed_at,
               CONCAT(u.first_name, ' ', u.last_name) AS owner_name
        FROM okr_objectives o
        JOIN projects p ON p.id = o.project_id
        JOIN users u ON u.id = o.owner_user_id
        WHERE o.id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, objectiveMapper(), objectiveId).stream().findFirst();
  }

  public Long insertObjective(
      Long projectId,
      Long ownerUserId,
      String title,
      String description,
      String status,
      LocalDate periodStart,
      LocalDate periodEnd
  ) {
    String sql = """
        INSERT INTO okr_objectives
          (company_id, project_id, owner_user_id, title, description, status, period_start, period_end, created_at, updated_at)
        VALUES ((SELECT company_id FROM projects WHERE id = ?), ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        RETURNING id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        projectId,
        projectId,
        ownerUserId,
        title,
        description,
        status,
        periodStart,
        periodEnd
    );
  }

  public void updateObjectiveConfidence(Long objectiveId, String confidenceLevel) {
    String sql = """
        UPDATE okr_objectives
        SET confidence_level = ?, updated_at = NOW()
        WHERE id = ?
        """;
    jdbcTemplateProvider.getJdbcTemplate().update(sql, confidenceLevel, objectiveId);
  }

  public void closeObjective(Long objectiveId, String status, BigDecimal finalScore) {
    String sql = """
        UPDATE okr_objectives
        SET status = ?, final_score = ?, closed_at = NOW(), updated_at = NOW()
        WHERE id = ?
        """;
    jdbcTemplateProvider.getJdbcTemplate().update(sql, status, finalScore, objectiveId);
  }

  public Long insertCheckin(
      Long objectiveId,
      Long authorUserId,
      Double progressPercent,
      String confidenceLevel,
      String note
  ) {
    String sql = """
        INSERT INTO okr_checkins
          (objective_id, author_user_id, progress_percent, confidence_level, note, created_at)
        VALUES (?, ?, ?, ?, ?, NOW())
        RETURNING id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        objectiveId,
        authorUserId,
        progressPercent,
        confidenceLevel,
        note
    );
  }

  public List<CheckinRow> findCheckinsByObjective(Long objectiveId) {
    String sql = """
        SELECT c.id,
               c.objective_id,
               c.author_user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS author_name,
               c.progress_percent,
               c.confidence_level,
               c.note,
               c.created_at
        FROM okr_checkins c
        JOIN users u ON u.id = c.author_user_id
        WHERE c.objective_id = ?
        ORDER BY c.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, checkinMapper(), objectiveId);
  }

  public Long insertInitiative(Long objectiveId, Long issueId) {
    String sql = """
        INSERT INTO okr_initiatives
          (objective_id, issue_id, created_at)
        VALUES (?, ?, NOW())
        ON CONFLICT DO NOTHING
        RETURNING id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        objectiveId,
        issueId
    );
  }

  public List<InitiativeRow> findInitiativesByObjective(Long objectiveId) {
    String sql = """
        SELECT i.id,
               i.objective_id,
               o.project_id,
               i.issue_id,
               iss.issue_key,
               iss.title AS issue_title,
               p.project_key,
               p.name AS project_name
        FROM okr_initiatives i
        JOIN okr_objectives o ON o.id = i.objective_id
        JOIN projects p ON p.id = o.project_id
        LEFT JOIN issues iss ON iss.id = i.issue_id
        WHERE i.objective_id = ?
        ORDER BY i.created_at DESC
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, initiativeMapper(), objectiveId);
  }

  public Long insertKeyResult(
      Long objectiveId,
      Long projectId,
      String title,
      BigDecimal targetValue,
      BigDecimal currentValue,
      String unit,
      String status
  ) {
    String sql = """
        INSERT INTO okr_key_results
          (objective_id, project_id, title, target_value, current_value, unit, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        RETURNING id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null,
        objectiveId,
        projectId,
        title,
        targetValue,
        currentValue,
        unit,
        status
    );
  }

  public Optional<KeyResultRow> findKeyResultById(Long keyResultId) {
    String sql = """
        SELECT kr.id,
               kr.objective_id,
               kr.project_id,
               p.project_key,
               p.name AS project_name,
               kr.title,
               kr.target_value,
               kr.current_value,
               kr.unit,
               kr.status
        FROM okr_key_results kr
        LEFT JOIN projects p ON p.id = kr.project_id
        WHERE kr.id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, keyResultMapper(), keyResultId).stream().findFirst();
  }

  public List<KeyResultRow> findKeyResultsByObjective(Long objectiveId) {
    String sql = """
        SELECT kr.id,
               kr.objective_id,
               kr.project_id,
               p.project_key,
               p.name AS project_name,
               kr.title,
               kr.target_value,
               kr.current_value,
               kr.unit,
               kr.status
        FROM okr_key_results kr
        LEFT JOIN projects p ON p.id = kr.project_id
        WHERE kr.objective_id = ?
        ORDER BY kr.id
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, keyResultMapper(), objectiveId);
  }

  public Map<Long, Double> fetchProgressByObjective() {
    String sql = """
        SELECT objective_id,
               AVG(LEAST(current_value / NULLIF(target_value, 0), 1)) AS progress
        FROM okr_key_results
        GROUP BY objective_id
        """;
    List<ProgressRow> rows = jdbcTemplateProvider.getJdbcTemplate().query(sql, progressMapper());
    return rows.stream().collect(Collectors.toMap(ProgressRow::objectiveId, ProgressRow::progress));
  }

  private RowMapper<ObjectiveRow> objectiveMapper() {
    return (rs, rowNum) -> new ObjectiveRow(
        rs.getLong("id"),
        rs.getLong("company_id"),
        rs.getLong("project_id"),
        rs.getString("project_key"),
        rs.getString("project_name"),
        rs.getLong("owner_user_id"),
        rs.getString("title"),
        rs.getString("description"),
        rs.getString("status"),
        rs.getDate("period_start").toLocalDate(),
        rs.getDate("period_end").toLocalDate(),
        rs.getString("owner_name"),
        rs.getString("confidence_level"),
        rs.getBigDecimal("final_score"),
        rs.getTimestamp("closed_at") != null ? rs.getTimestamp("closed_at").toLocalDateTime() : null
    );
  }

  private RowMapper<KeyResultRow> keyResultMapper() {
    return (rs, rowNum) -> new KeyResultRow(
        rs.getLong("id"),
        rs.getLong("objective_id"),
        (Long) rs.getObject("project_id"),
        rs.getString("project_key"),
        rs.getString("project_name"),
        rs.getString("title"),
        rs.getBigDecimal("target_value"),
        rs.getBigDecimal("current_value"),
        rs.getString("unit"),
        rs.getString("status")
    );
  }

  private RowMapper<CheckinRow> checkinMapper() {
    return (rs, rowNum) -> new CheckinRow(
        rs.getLong("id"),
        rs.getLong("objective_id"),
        rs.getLong("author_user_id"),
        rs.getString("author_name"),
        rs.getBigDecimal("progress_percent") != null
            ? rs.getBigDecimal("progress_percent").doubleValue()
            : null,
        rs.getString("confidence_level"),
        rs.getString("note"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }

  private RowMapper<InitiativeRow> initiativeMapper() {
    return (rs, rowNum) -> new InitiativeRow(
        rs.getLong("id"),
        rs.getLong("objective_id"),
        rs.getLong("project_id"),
        (Long) rs.getObject("issue_id"),
        rs.getString("issue_key"),
        rs.getString("issue_title"),
        rs.getString("project_key"),
        rs.getString("project_name")
    );
  }

  private RowMapper<ProgressRow> progressMapper() {
    return (rs, rowNum) -> new ProgressRow(
        rs.getLong("objective_id"),
        rs.getDouble("progress")
    );
  }

  public record ObjectiveRow(
      Long id,
      Long companyId,
      Long projectId,
      String projectKey,
      String projectName,
      Long ownerUserId,
      String title,
      String description,
      String status,
      LocalDate periodStart,
      LocalDate periodEnd,
      String ownerName,
      String confidenceLevel,
      BigDecimal finalScore,
      java.time.LocalDateTime closedAt
  ) {}

  public record KeyResultRow(
      Long id,
      Long objectiveId,
      Long projectId,
      String projectKey,
      String projectName,
      String title,
      BigDecimal targetValue,
      BigDecimal currentValue,
      String unit,
      String status
  ) {}

  public record CheckinRow(
      Long id,
      Long objectiveId,
      Long authorUserId,
      String authorName,
      Double progressPercent,
      String confidenceLevel,
      String note,
      java.time.LocalDateTime createdAt
  ) {}

  public record InitiativeRow(
      Long id,
      Long objectiveId,
      Long projectId,
      Long issueId,
      String issueKey,
      String issueTitle,
      String projectKey,
      String projectName
  ) {}

  public record ProgressRow(Long objectiveId, double progress) {}
}
