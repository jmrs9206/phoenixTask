ALTER TABLE issues
    ADD COLUMN kanban_position BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_issues_project_status_position
    ON issues (project_id, status, kanban_position, id);

CREATE INDEX idx_issues_project_sprint_status_position
    ON issues (project_id, sprint_id, status, kanban_position, id);
