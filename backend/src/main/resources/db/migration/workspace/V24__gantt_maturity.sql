CREATE TABLE gantt_issue_dependencies (
  id BIGSERIAL PRIMARY KEY,
  predecessor_issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
  successor_issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
  dependency_type VARCHAR(16) NOT NULL DEFAULT 'FS',
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by_user_id BIGINT NULL REFERENCES users(id),
  CONSTRAINT chk_gantt_dependency_not_self CHECK (predecessor_issue_id <> successor_issue_id)
);

CREATE UNIQUE INDEX ux_gantt_issue_dependencies
  ON gantt_issue_dependencies(predecessor_issue_id, successor_issue_id);

CREATE INDEX idx_gantt_issue_dependencies_successor
  ON gantt_issue_dependencies(successor_issue_id);

CREATE TABLE gantt_project_baselines (
  project_id BIGINT PRIMARY KEY REFERENCES projects(id) ON DELETE CASCADE,
  baseline_start_date DATE NULL,
  baseline_end_date DATE NULL,
  captured_at TIMESTAMP NOT NULL DEFAULT NOW(),
  captured_by_user_id BIGINT NULL REFERENCES users(id)
);

CREATE TABLE gantt_issue_baselines (
  issue_id BIGINT PRIMARY KEY REFERENCES issues(id) ON DELETE CASCADE,
  baseline_start_date DATE NULL,
  baseline_end_date DATE NULL,
  captured_at TIMESTAMP NOT NULL DEFAULT NOW(),
  captured_by_user_id BIGINT NULL REFERENCES users(id)
);

INSERT INTO gantt_project_baselines (project_id, baseline_start_date, baseline_end_date, captured_at)
SELECT id, planned_start_date, planned_end_date, NOW()
FROM projects
ON CONFLICT (project_id) DO NOTHING;

INSERT INTO gantt_issue_baselines (issue_id, baseline_start_date, baseline_end_date, captured_at)
SELECT id, planned_start_date, due_date, NOW()
FROM issues
ON CONFLICT (issue_id) DO NOTHING;

INSERT INTO gantt_issue_dependencies (predecessor_issue_id, successor_issue_id, dependency_type)
SELECT p.id, s.id, 'FS'
FROM issues p
JOIN issues s ON s.project_id = p.project_id
WHERE p.issue_key = 'PTW-1' AND s.issue_key = 'PTW-2'
ON CONFLICT (predecessor_issue_id, successor_issue_id) DO NOTHING;

INSERT INTO gantt_issue_dependencies (predecessor_issue_id, successor_issue_id, dependency_type)
SELECT p.id, s.id, 'FS'
FROM issues p
JOIN issues s ON s.project_id = p.project_id
WHERE p.issue_key = 'CORE-1' AND s.issue_key = 'CORE-2'
ON CONFLICT (predecessor_issue_id, successor_issue_id) DO NOTHING;
