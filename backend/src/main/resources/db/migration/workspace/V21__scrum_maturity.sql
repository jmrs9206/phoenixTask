CREATE TABLE sprint_commitment_snapshots (
  sprint_id BIGINT PRIMARY KEY REFERENCES sprints(id) ON DELETE CASCADE,
  captured_at TIMESTAMP NOT NULL DEFAULT NOW(),
  committed_count INTEGER NOT NULL
);

CREATE TABLE sprint_commitments (
  id BIGSERIAL PRIMARY KEY,
  sprint_id BIGINT NOT NULL REFERENCES sprints(id) ON DELETE CASCADE,
  issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
  captured_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (sprint_id, issue_id)
);

CREATE INDEX idx_sprint_commitments_sprint_id ON sprint_commitments(sprint_id);

CREATE TABLE sprint_burndown_points (
  id BIGSERIAL PRIMARY KEY,
  sprint_id BIGINT NOT NULL REFERENCES sprints(id) ON DELETE CASCADE,
  point_date DATE NOT NULL,
  committed_count INTEGER NOT NULL,
  remaining_count INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (sprint_id, point_date)
);

CREATE INDEX idx_sprint_burndown_points_sprint_id ON sprint_burndown_points(sprint_id);
