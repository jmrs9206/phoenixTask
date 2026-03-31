CREATE TABLE sprints (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id),
  name VARCHAR(120) NOT NULL,
  goal VARCHAR(255),
  status VARCHAR(16) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (project_id, name)
);

ALTER TABLE sprints
  ADD CONSTRAINT chk_sprints_status
    CHECK (status IN ('PLANNED', 'ACTIVE', 'COMPLETED'));

CREATE INDEX idx_sprints_project_status ON sprints(project_id, status);

CREATE TABLE okr_objectives (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT NOT NULL REFERENCES company(id),
  owner_user_id BIGINT NOT NULL REFERENCES users(id),
  title VARCHAR(200) NOT NULL,
  description VARCHAR(500),
  status VARCHAR(16) NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE okr_objectives
  ADD CONSTRAINT chk_okr_objective_status
    CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED'));

CREATE TABLE okr_key_results (
  id BIGSERIAL PRIMARY KEY,
  objective_id BIGINT NOT NULL REFERENCES okr_objectives(id),
  project_id BIGINT NULL REFERENCES projects(id),
  title VARCHAR(200) NOT NULL,
  target_value NUMERIC(12,2) NOT NULL,
  current_value NUMERIC(12,2) NOT NULL,
  unit VARCHAR(32) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE okr_key_results
  ADD CONSTRAINT chk_okr_key_result_status
    CHECK (status IN ('ON_TRACK', 'AT_RISK', 'OFF_TRACK', 'COMPLETED'));

ALTER TABLE projects
  ADD COLUMN planned_start_date DATE NULL,
  ADD COLUMN planned_end_date DATE NULL;

ALTER TABLE issues
  ADD COLUMN sprint_id BIGINT NULL REFERENCES sprints(id),
  ADD COLUMN planned_start_date DATE NULL,
  ADD COLUMN due_date DATE NULL;

CREATE INDEX idx_issues_sprint_id ON issues(sprint_id);
CREATE INDEX idx_issues_due_date ON issues(due_date);
CREATE INDEX idx_issues_planned_start_date ON issues(planned_start_date);
CREATE INDEX idx_projects_planned_end_date ON projects(planned_end_date);

UPDATE projects
SET planned_start_date = DATE '2026-03-01',
    planned_end_date = DATE '2026-04-30'
WHERE project_key = 'PTW';

UPDATE projects
SET planned_start_date = DATE '2026-03-05',
    planned_end_date = DATE '2026-05-15'
WHERE project_key = 'CORE';

UPDATE projects
SET planned_start_date = DATE '2026-03-10',
    planned_end_date = DATE '2026-04-20'
WHERE project_key = 'CSOPS';

INSERT INTO sprints (project_id, name, goal, status, start_date, end_date, created_at, updated_at)
SELECT p.id,
       'Sprint 1',
       'Stabilize onboarding and settings',
       'ACTIVE',
       DATE '2026-03-03',
       DATE '2026-03-17',
       NOW(),
       NOW()
FROM projects p
WHERE p.project_key = 'PTW'
  AND NOT EXISTS (
    SELECT 1 FROM sprints s WHERE s.project_id = p.id AND s.name = 'Sprint 1'
  );

INSERT INTO sprints (project_id, name, goal, status, start_date, end_date, created_at, updated_at)
SELECT p.id,
       'Sprint 2',
       'Improve issue workflow',
       'PLANNED',
       DATE '2026-03-18',
       DATE '2026-04-01',
       NOW(),
       NOW()
FROM projects p
WHERE p.project_key = 'PTW'
  AND NOT EXISTS (
    SELECT 1 FROM sprints s WHERE s.project_id = p.id AND s.name = 'Sprint 2'
  );

INSERT INTO okr_objectives (
  company_id,
  owner_user_id,
  title,
  description,
  status,
  period_start,
  period_end,
  created_at,
  updated_at
)
SELECT c.id,
       u.id,
       'Increase onboarding completion',
       NULL,
       'ACTIVE',
       DATE '2026-03-01',
       DATE '2026-06-30',
       NOW(),
       NOW()
FROM company c
JOIN users u ON u.email = 'sofia.ramos@phoenixtask.demo'
WHERE NOT EXISTS (
  SELECT 1 FROM okr_objectives o WHERE o.title = 'Increase onboarding completion'
);

INSERT INTO okr_key_results (
  objective_id,
  project_id,
  title,
  target_value,
  current_value,
  unit,
  status,
  created_at,
  updated_at
)
SELECT o.id,
       p.id,
       'Onboarding completion rate to 70%',
       70,
       42,
       'percent',
       'ON_TRACK',
       NOW(),
       NOW()
FROM okr_objectives o
LEFT JOIN projects p ON p.project_key = 'PTW'
WHERE o.title = 'Increase onboarding completion'
  AND NOT EXISTS (
    SELECT 1 FROM okr_key_results kr WHERE kr.title = 'Onboarding completion rate to 70%'
  );

INSERT INTO okr_key_results (
  objective_id,
  project_id,
  title,
  target_value,
  current_value,
  unit,
  status,
  created_at,
  updated_at
)
SELECT o.id,
       p.id,
       'Time-to-first-project under 10 minutes',
       10,
       16,
       'minutes',
       'AT_RISK',
       NOW(),
       NOW()
FROM okr_objectives o
LEFT JOIN projects p ON p.project_key = 'PTW'
WHERE o.title = 'Increase onboarding completion'
  AND NOT EXISTS (
    SELECT 1 FROM okr_key_results kr WHERE kr.title = 'Time-to-first-project under 10 minutes'
  );

INSERT INTO issues (
  project_id,
  issue_key,
  title,
  description,
  status,
  priority,
  reporter_user_id,
  assignee_user_id,
  created_at,
  updated_at
)
SELECT p.id,
       'PTW-1',
       'Onboarding checklist review',
       'Review onboarding checklist and align with product onboarding flow.',
       'OPEN',
       'MEDIUM',
       r.id,
       a.id,
       NOW(),
       NOW()
FROM projects p
JOIN users r ON r.email = 'sofia.ramos@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'lucia.vega@phoenixtask.demo'
WHERE p.project_key = 'PTW'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'PTW-1');

INSERT INTO issues (
  project_id,
  issue_key,
  title,
  description,
  status,
  priority,
  reporter_user_id,
  assignee_user_id,
  created_at,
  updated_at
)
SELECT p.id,
       'PTW-2',
       'Settings quick-start guide',
       'Draft a concise settings quick-start guide for new teams.',
       'IN_PROGRESS',
       'HIGH',
       r.id,
       a.id,
       NOW(),
       NOW()
FROM projects p
JOIN users r ON r.email = 'mateo.cruz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'lucia.vega@phoenixtask.demo'
WHERE p.project_key = 'PTW'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'PTW-2');

INSERT INTO issues (
  project_id,
  issue_key,
  title,
  description,
  status,
  priority,
  reporter_user_id,
  assignee_user_id,
  created_at,
  updated_at
)
SELECT p.id,
       'CORE-1',
       'Tenant header validation audit',
       'Audit workspace endpoints for tenant header validation coverage.',
       'BLOCKED',
       'CRITICAL',
       r.id,
       a.id,
       NOW(),
       NOW()
FROM projects p
JOIN users r ON r.email = 'mateo.cruz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'elena.torres@phoenixtask.demo'
WHERE p.project_key = 'CORE'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'CORE-1');

INSERT INTO issues (
  project_id,
  issue_key,
  title,
  description,
  status,
  priority,
  reporter_user_id,
  assignee_user_id,
  created_at,
  updated_at
)
SELECT p.id,
       'CSOPS-1',
       'Customer handoff template',
       'Prepare a standardized customer handoff template for support.',
       'DONE',
       'LOW',
       r.id,
       a.id,
       NOW(),
       NOW()
FROM projects p
JOIN users r ON r.email = 'pablo.ruiz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'pablo.ruiz@phoenixtask.demo'
WHERE p.project_key = 'CSOPS'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'CSOPS-1');

UPDATE issues
SET planned_start_date = DATE '2026-03-03',
    due_date = DATE '2026-03-08'
WHERE issue_key = 'PTW-1';

UPDATE issues
SET planned_start_date = DATE '2026-03-06',
    due_date = DATE '2026-03-15'
WHERE issue_key = 'PTW-2';

UPDATE issues
SET planned_start_date = DATE '2026-03-07',
    due_date = DATE '2026-03-21'
WHERE issue_key = 'CORE-1';

UPDATE issues
SET planned_start_date = DATE '2026-03-12',
    due_date = DATE '2026-03-26'
WHERE issue_key = 'CSOPS-1';

UPDATE issues
SET sprint_id = (
  SELECT s.id
  FROM sprints s
  JOIN projects p ON p.id = s.project_id
  WHERE p.project_key = 'PTW' AND s.name = 'Sprint 1'
)
WHERE issue_key IN ('PTW-1', 'PTW-2');

UPDATE projects p
SET issue_counter = GREATEST(
  p.issue_counter,
  COALESCE(
    (
      SELECT MAX(CAST(SPLIT_PART(i.issue_key, '-', 2) AS BIGINT))
      FROM issues i
      WHERE i.project_id = p.id
    ),
    0
  )
);
