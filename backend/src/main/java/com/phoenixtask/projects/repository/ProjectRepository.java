package com.phoenixtask.projects.repository;

import com.phoenixtask.projects.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Project> rowMapper = (rs, rowNum) -> new Project(
        rs.getLong("id"),
        rs.getString("project_key"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getString("status"),
        rs.getLong("owner_user_id"),
        rs.getDate("planned_start_date") != null ? rs.getDate("planned_start_date").toLocalDate() : null,
        rs.getDate("planned_end_date") != null ? rs.getDate("planned_end_date").toLocalDate() : null,
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
    );

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Project> findAll() {
        return jdbcTemplate.query("SELECT * FROM projects ORDER BY created_at DESC", rowMapper);
    }

    public Optional<Project> findById(Long id) {
        var results = jdbcTemplate.query("SELECT * FROM projects WHERE id = ?", rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public Optional<Project> findByKey(String key) {
        var results = jdbcTemplate.query("SELECT * FROM projects WHERE project_key = ?", rowMapper, key);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public Project save(Project project) {
        if (project.id() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO projects (project_key, name, description, status, owner_user_id, planned_start_date, planned_end_date) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"id"});
                ps.setString(1, project.projectKey());
                ps.setString(2, project.name());
                ps.setString(3, project.description());
                ps.setString(4, project.status() != null ? project.status() : "DRAFT");
                ps.setLong(5, project.ownerUserId());
                ps.setObject(6, project.plannedStartDate());
                ps.setObject(7, project.plannedEndDate());
                return ps;
            }, keyHolder);
            return findById(keyHolder.getKey().longValue()).orElseThrow();
        } else {
            jdbcTemplate.update(
                "UPDATE projects SET project_key=?, name=?, description=?, status=?, owner_user_id=?, planned_start_date=?, planned_end_date=? WHERE id=?",
                project.projectKey(), project.name(), project.description(), project.status(), project.ownerUserId(),
                project.plannedStartDate(), project.plannedEndDate(), project.id()
            );
            return project;
        }
    }

    public void updateStatus(Long id, String status) {
        jdbcTemplate.update("UPDATE projects SET status = ? WHERE id = ?", status, id);
    }
}
