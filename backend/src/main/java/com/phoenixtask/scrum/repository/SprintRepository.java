package com.phoenixtask.scrum.repository;

import com.phoenixtask.scrum.model.Sprint;
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
public class SprintRepository {
    private final JdbcTemplate jdbcTemplate;

    public SprintRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Sprint> rowMapper = (rs, rowNum) -> {
        Sprint sprint = new Sprint();
        sprint.setId(rs.getLong("id"));
        sprint.setProjectId(rs.getLong("project_id"));
        sprint.setName(rs.getString("name"));
        sprint.setGoal(rs.getString("goal"));
        sprint.setStatus(rs.getString("status"));
        sprint.setStartDate(rs.getDate("start_date").toLocalDate());
        sprint.setEndDate(rs.getDate("end_date").toLocalDate());
        sprint.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        sprint.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return sprint;
    };

    public List<Sprint> findAllByProjectId(Long projectId) {
        String sql = "SELECT * FROM sprints WHERE project_id = ?";
        return jdbcTemplate.query(sql, rowMapper, projectId);
    }

    public Optional<Sprint> findById(Long id) {
        String sql = "SELECT * FROM sprints WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public Optional<Sprint> findActiveSprintByProjectId(Long projectId) {
        String sql = "SELECT * FROM sprints WHERE project_id = ? AND status = 'ACTIVE'";
        return jdbcTemplate.query(sql, rowMapper, projectId).stream().findFirst();
    }

    public boolean existsByNameInProject(Long projectId, String name) {
        String sql = "SELECT COUNT(*) FROM sprints WHERE project_id = ? AND name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId, name);
        return count != null && count > 0;
    }

    public Sprint save(Sprint sprint) {
        String sql = "INSERT INTO sprints (project_id, name, goal, status, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, sprint.getProjectId());
            ps.setString(2, sprint.getName());
            ps.setString(3, sprint.getGoal());
            ps.setString(4, sprint.getStatus());
            ps.setObject(5, sprint.getStartDate());
            ps.setObject(6, sprint.getEndDate());
            return ps;
        }, keyHolder);

        sprint.setId(keyHolder.getKey().longValue());
        return sprint;
    }

    public void update(Sprint sprint) {
        String sql = "UPDATE sprints SET name = ?, goal = ?, start_date = ?, end_date = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getId());
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE sprints SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    public void updateIssueSprint(Long issueId, Long sprintId) {
        String sql = "UPDATE issues SET sprint_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, sprintId, issueId);
    }
}
