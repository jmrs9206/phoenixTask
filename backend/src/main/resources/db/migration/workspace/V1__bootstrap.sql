CREATE TABLE workspace_bootstrap_marker (
  id BIGSERIAL PRIMARY KEY,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
