CREATE TABLE IF NOT EXISTS git_integrations (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
  provider VARCHAR(20) NOT NULL,
  label VARCHAR(120) NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  token_prefix VARCHAR(16) NOT NULL,
  webhook_secret VARCHAR(128) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS git_repositories (
  id BIGSERIAL PRIMARY KEY,
  integration_id BIGINT NOT NULL REFERENCES git_integrations(id) ON DELETE CASCADE,
  project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  repo_owner VARCHAR(120) NOT NULL,
  repo_name VARCHAR(200) NOT NULL,
  default_branch VARCHAR(100),
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS git_repositories_unique_repo
  ON git_repositories (integration_id, repo_owner, repo_name);

CREATE TABLE IF NOT EXISTS git_webhook_events (
  id BIGSERIAL PRIMARY KEY,
  integration_id BIGINT NOT NULL REFERENCES git_integrations(id) ON DELETE CASCADE,
  provider VARCHAR(20) NOT NULL,
  event_type VARCHAR(120) NOT NULL,
  delivery_id VARCHAR(120),
  repo_full_name VARCHAR(220),
  signature_valid BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);
