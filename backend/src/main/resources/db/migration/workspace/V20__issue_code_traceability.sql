CREATE TABLE IF NOT EXISTS issue_code_links (
  id BIGSERIAL PRIMARY KEY,
  issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
  project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  integration_id BIGINT NOT NULL REFERENCES git_integrations(id) ON DELETE CASCADE,
  repo_id BIGINT NOT NULL REFERENCES git_repositories(id) ON DELETE CASCADE,
  provider VARCHAR(20) NOT NULL,
  artifact_type VARCHAR(32) NOT NULL,
  external_id VARCHAR(200) NOT NULL,
  title VARCHAR(400),
  url VARCHAR(600),
  author_name VARCHAR(200),
  external_created_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS issue_code_links_unique
  ON issue_code_links (issue_id, repo_id, artifact_type, external_id);

CREATE INDEX IF NOT EXISTS issue_code_links_issue_id_idx
  ON issue_code_links (issue_id);

CREATE INDEX IF NOT EXISTS issue_code_links_repo_id_idx
  ON issue_code_links (repo_id);
