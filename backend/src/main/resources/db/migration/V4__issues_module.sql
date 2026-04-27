CREATE TABLE issues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    issue_number INT NOT NULL,
    issue_key VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'BACKLOG',
    priority VARCHAR(20) NOT NULL DEFAULT 'LOW',
    reporter_user_id BIGINT NOT NULL,
    assignee_user_id BIGINT,
    planned_start_date DATE,
    due_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_issues_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_issues_reporter FOREIGN KEY (reporter_user_id) REFERENCES users (id),
    CONSTRAINT fk_issues_assignee FOREIGN KEY (assignee_user_id) REFERENCES users (id),
    CONSTRAINT uk_issues_project_number UNIQUE (project_id, issue_number),
    CONSTRAINT uk_issues_key UNIQUE (issue_key)
) ENGINE=InnoDB;

CREATE INDEX idx_issues_project ON issues (project_id);
CREATE INDEX idx_issues_status ON issues (status);
CREATE INDEX idx_issues_reporter ON issues (reporter_user_id);
CREATE INDEX idx_issues_assignee ON issues (assignee_user_id);
