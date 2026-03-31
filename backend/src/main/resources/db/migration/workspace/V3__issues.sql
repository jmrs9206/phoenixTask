ALTER TABLE projects
  ADD COLUMN issue_counter BIGINT NOT NULL DEFAULT 0;

CREATE TABLE issues (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id),
  issue_key VARCHAR(32) NOT NULL UNIQUE,
  title VARCHAR(200) NOT NULL,
  description VARCHAR(1000),
  status VARCHAR(32) NOT NULL,
  priority VARCHAR(32) NOT NULL,
  reporter_user_id BIGINT NOT NULL REFERENCES users(id),
  assignee_user_id BIGINT NULL REFERENCES users(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_issues_project_id ON issues(project_id);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_assignee_user_id ON issues(assignee_user_id);
