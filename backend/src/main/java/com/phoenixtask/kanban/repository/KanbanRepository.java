package com.phoenixtask.kanban.repository;

import com.phoenixtask.kanban.model.KanbanIssueCard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class KanbanRepository {
    private final JdbcTemplate jdbcTemplate;

    public KanbanRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<KanbanIssueCard> cardRowMapper = (rs, rowNum) -> {
        KanbanIssueCard card = new KanbanIssueCard();
        card.setId(rs.getLong("id"));
        card.setIssueKey(rs.getString("issue_key"));
        card.setTitle(rs.getString("title"));
        card.setStatus(rs.getString("status"));
        card.setPriority(rs.getString("priority"));
        card.setAssigneeUserId(rs.getObject("assignee_user_id", Long.class));
        card.setSprintId(rs.getObject("sprint_id", Long.class));
        card.setKanbanPosition(rs.getLong("kanban_position"));
        return card;
    };

    public List<KanbanIssueCard> findCardsByProject(Long projectId, Long sprintId) {
        String baseSql = "SELECT id, issue_key, title, status, priority, assignee_user_id, sprint_id, kanban_position FROM issues WHERE project_id = ?";
        if (sprintId != null) {
            String sql = baseSql + " AND sprint_id = ? ORDER BY status, kanban_position, id";
            return jdbcTemplate.query(sql, cardRowMapper, projectId, sprintId);
        } else {
            String sql = baseSql + " ORDER BY status, kanban_position, id";
            return jdbcTemplate.query(sql, cardRowMapper, projectId);
        }
    }

    public Optional<KanbanIssueCard> findCardById(Long issueId) {
        String sql = "SELECT id, issue_key, title, status, priority, assignee_user_id, sprint_id, kanban_position " +
                     "FROM issues WHERE id = ?";
        return jdbcTemplate.query(sql, cardRowMapper, issueId).stream().findFirst();
    }

    public void updatePositionAndStatus(Long issueId, String status, Long position) {
        String sql = "UPDATE issues SET status = ?, kanban_position = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, position, issueId);
    }

    public List<KanbanIssueCard> findCardsByColumn(Long projectId, String status) {
        String sql = "SELECT id, issue_key, title, status, priority, assignee_user_id, sprint_id, kanban_position " +
                     "FROM issues WHERE project_id = ? AND status = ? ORDER BY kanban_position, id";
        return jdbcTemplate.query(sql, cardRowMapper, projectId, status);
    }

    public void updatePosition(Long issueId, Long position) {
        String sql = "UPDATE issues SET kanban_position = ? WHERE id = ?";
        jdbcTemplate.update(sql, position, issueId);
    }
}
