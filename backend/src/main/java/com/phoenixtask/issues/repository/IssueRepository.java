package com.phoenixtask.issues.repository;

import com.phoenixtask.issues.model.Issue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class IssueRepository {
    private final JdbcTemplate jdbcTemplate;

    public IssueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Issue> rowMapper = (rs, rowNum) -> {
        Issue issue = new Issue();
        issue.setId(rs.getLong("id"));
        issue.setProjectId(rs.getLong("project_id"));
        issue.setIssueNumber(rs.getInt("issue_number"));
        issue.setIssueKey(rs.getString("issue_key"));
        issue.setTitle(rs.getString("title"));
        issue.setDescription(rs.getString("description"));
        issue.setStatus(rs.getString("status"));
        issue.setPriority(rs.getString("priority"));
        issue.setReporterUserId(rs.getLong("reporter_user_id"));
        issue.setAssigneeUserId(rs.getObject("assignee_user_id", Long.class));
        issue.setPlannedStartDate(rs.getDate("planned_start_date") != null ? rs.getDate("planned_start_date").toLocalDate() : null);
        issue.setDueDate(rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null);
        issue.setSprintId(rs.getObject("sprint_id", Long.class));
        issue.setKanbanPosition(rs.getLong("kanban_position"));
        issue.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        issue.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return issue;
    };

    public List<Issue> findAllByProjectId(Long projectId) {
        String sql = "SELECT * FROM issues WHERE project_id = ?";
        return jdbcTemplate.query(sql, rowMapper, projectId);
    }

    public Optional<Issue> findById(Long id) {
        String sql = "SELECT * FROM issues WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public Issue save(Issue issue) {
        String sql = "INSERT INTO issues (project_id, issue_number, issue_key, title, description, status, priority, reporter_user_id, assignee_user_id, planned_start_date, due_date, kanban_position) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, issue.getProjectId());
            ps.setInt(2, issue.getIssueNumber());
            ps.setString(3, issue.getIssueKey());
            ps.setString(4, issue.getTitle());
            ps.setString(5, issue.getDescription());
            ps.setString(6, issue.getStatus());
            ps.setString(7, issue.getPriority());
            ps.setLong(8, issue.getReporterUserId());
            ps.setObject(9, issue.getAssigneeUserId());
            ps.setObject(10, issue.getPlannedStartDate());
            ps.setObject(11, issue.getDueDate());
            ps.setLong(12, issue.getKanbanPosition() != null ? issue.getKanbanPosition() : 0L);
            return ps;
        }, keyHolder);

        issue.setId(keyHolder.getKey().longValue());
        return issue;
    }

    public void update(Issue issue) {
        String sql = "UPDATE issues SET title = ?, description = ?, status = ?, priority = ?, assignee_user_id = ?, planned_start_date = ?, due_date = ?, kanban_position = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getAssigneeUserId(),
                issue.getPlannedStartDate(),
                issue.getDueDate(),
                issue.getKanbanPosition(),
                issue.getId());
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE issues SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    public int getNextIssueNumber(Long projectId) {
        String sql = "SELECT COALESCE(MAX(issue_number), 0) + 1 FROM issues WHERE project_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, projectId);
    }
}
