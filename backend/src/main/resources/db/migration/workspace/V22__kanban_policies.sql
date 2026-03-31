CREATE TABLE IF NOT EXISTS kanban_column_policies (
  project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  status VARCHAR(32) NOT NULL,
  wip_limit INTEGER,
  policy_text VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),
  PRIMARY KEY (project_id, status)
);

INSERT INTO kanban_column_policies (project_id, status, wip_limit, policy_text)
SELECT p.id,
       s.status,
       CASE s.status
         WHEN 'OPEN' THEN 12
         WHEN 'IN_PROGRESS' THEN 5
         WHEN 'BLOCKED' THEN 3
         WHEN 'DONE' THEN 999
         ELSE NULL
       END,
       CASE s.status
         WHEN 'OPEN' THEN 'Clarify scope before pulling into progress.'
         WHEN 'IN_PROGRESS' THEN 'Limit active work and keep focus tight.'
         WHEN 'BLOCKED' THEN 'Escalate blockers within 24h.'
         WHEN 'DONE' THEN 'Verify outcomes and close the loop.'
         ELSE NULL
       END
FROM projects p
CROSS JOIN (VALUES ('OPEN'), ('IN_PROGRESS'), ('BLOCKED'), ('DONE')) AS s(status)
ON CONFLICT (project_id, status) DO NOTHING;
