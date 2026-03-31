package com.phoenixtask.workspace.infrastructure.persistence;

import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceSprintMetricsRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceSprintMetricsRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public Optional<CommitmentSnapshotRow> findSnapshot(Long sprintId) {
    String sql = """
        SELECT sprint_id, captured_at, committed_count
        FROM sprint_commitment_snapshots
        WHERE sprint_id = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, snapshotMapper(), sprintId).stream().findFirst();
  }

  public CommitmentSnapshotRow createSnapshot(Long sprintId, int committedCount) {
    String sql = """
        INSERT INTO sprint_commitment_snapshots (sprint_id, captured_at, committed_count)
        VALUES (?, NOW(), ?)
        RETURNING sprint_id, captured_at, committed_count
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        snapshotMapper(),
        sprintId,
        committedCount
    ).stream().findFirst().orElseThrow();
  }

  public void insertCommitments(Long sprintId, List<Long> issueIds, LocalDateTime capturedAt) {
    if (issueIds.isEmpty()) {
      return;
    }
    String sql = """
        INSERT INTO sprint_commitments (sprint_id, issue_id, captured_at)
        VALUES (?, ?, ?)
        ON CONFLICT (sprint_id, issue_id) DO NOTHING
        """;
    var jdbc = jdbcTemplateProvider.getJdbcTemplate();
    for (Long issueId : issueIds) {
      jdbc.update(sql, sprintId, issueId, capturedAt);
    }
  }

  public List<Long> findCommittedIssueIds(Long sprintId) {
    String sql = "SELECT issue_id FROM sprint_commitments WHERE sprint_id = ?";
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        (rs, rowNum) -> rs.getLong("issue_id"),
        sprintId
    );
  }

  public int countCompletedCommitted(Long sprintId) {
    String sql = """
        SELECT COUNT(*)
        FROM sprint_commitments sc
        JOIN issues i ON i.id = sc.issue_id
        WHERE sc.sprint_id = ?
          AND i.status = 'DONE'
        """;
    Integer count = jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getInt(1) : 0,
        sprintId
    );
    return count == null ? 0 : count;
  }

  public Optional<BurndownPointRow> findBurndownPoint(Long sprintId, LocalDate date) {
    String sql = """
        SELECT sprint_id, point_date, committed_count, remaining_count, created_at
        FROM sprint_burndown_points
        WHERE sprint_id = ? AND point_date = ?
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, burndownMapper(), sprintId, date).stream().findFirst();
  }

  public List<BurndownPointRow> findBurndownPoints(Long sprintId) {
    String sql = """
        SELECT sprint_id, point_date, committed_count, remaining_count, created_at
        FROM sprint_burndown_points
        WHERE sprint_id = ?
        ORDER BY point_date
        """;
    return jdbcTemplateProvider.getJdbcTemplate().query(sql, burndownMapper(), sprintId);
  }

  public void insertBurndownPoint(Long sprintId, LocalDate date, int committedCount, int remainingCount) {
    String sql = """
        INSERT INTO sprint_burndown_points (sprint_id, point_date, committed_count, remaining_count, created_at)
        VALUES (?, ?, ?, ?, NOW())
        ON CONFLICT (sprint_id, point_date) DO NOTHING
        """;
    jdbcTemplateProvider.getJdbcTemplate().update(sql, sprintId, date, committedCount, remainingCount);
  }

  private RowMapper<CommitmentSnapshotRow> snapshotMapper() {
    return (rs, rowNum) -> new CommitmentSnapshotRow(
        rs.getLong("sprint_id"),
        rs.getTimestamp("captured_at").toLocalDateTime(),
        rs.getInt("committed_count")
    );
  }

  private RowMapper<BurndownPointRow> burndownMapper() {
    return (rs, rowNum) -> new BurndownPointRow(
        rs.getLong("sprint_id"),
        rs.getDate("point_date").toLocalDate(),
        rs.getInt("committed_count"),
        rs.getInt("remaining_count"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );
  }

  public record CommitmentSnapshotRow(Long sprintId, LocalDateTime capturedAt, int committedCount) {}

  public record BurndownPointRow(
      Long sprintId,
      LocalDate pointDate,
      int committedCount,
      int remainingCount,
      LocalDateTime createdAt
  ) {}
}
