CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    owner_user_id BIGINT NOT NULL,
    planned_start_date DATE,
    planned_end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_projects_owner FOREIGN KEY (owner_user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE INDEX idx_projects_status ON projects (status);
CREATE INDEX idx_projects_owner ON projects (owner_user_id);
