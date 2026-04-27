CREATE TABLE sprints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    goal TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sprints_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT uk_sprints_project_name UNIQUE (project_id, name)
) ENGINE=InnoDB;

CREATE INDEX idx_sprints_project ON sprints (project_id);
CREATE INDEX idx_sprints_status ON sprints (status);

ALTER TABLE issues
    ADD COLUMN sprint_id BIGINT NULL,
    ADD CONSTRAINT fk_issues_sprint FOREIGN KEY (sprint_id) REFERENCES sprints (id);

CREATE INDEX idx_issues_sprint ON issues (sprint_id);
