-- Normalize issue status, priority, and category to the Phase Y taxonomy.

UPDATE issues
SET status = CASE UPPER(status)
  WHEN 'OPEN' THEN 'BACKLOG'
  WHEN 'IN_PROGRESS' THEN 'IN_PROGRESS'
  WHEN 'BLOCKED' THEN 'BLOCKED'
  WHEN 'DONE' THEN 'DONE'
  ELSE UPPER(status)
END
WHERE status IS NOT NULL;

UPDATE issues
SET priority = CASE UPPER(priority)
  WHEN 'CRITICAL' THEN 'EPIC'
  WHEN 'HIGH' THEN 'HIGH'
  WHEN 'MEDIUM' THEN 'MEDIUM'
  WHEN 'LOW' THEN 'LOW'
  ELSE UPPER(priority)
END
WHERE priority IS NOT NULL;

UPDATE issues
SET category = CASE UPPER(category)
  WHEN 'GENERAL' THEN 'TASK'
  WHEN 'OPS' THEN 'TECHNICAL_DEBT'
  WHEN 'FEATURE' THEN 'FEATURE'
  WHEN 'BUG' THEN 'BUG'
  WHEN 'IMPROVEMENT' THEN 'IMPROVEMENT'
  ELSE UPPER(category)
END
WHERE category IS NOT NULL;

UPDATE issues
SET category = 'TASK'
WHERE category IS NULL OR category = '';

ALTER TABLE issues
  ALTER COLUMN category SET DEFAULT 'TASK';

UPDATE kanban_column_policies
SET status = CASE UPPER(status)
  WHEN 'OPEN' THEN 'BACKLOG'
  WHEN 'IN_PROGRESS' THEN 'IN_PROGRESS'
  WHEN 'BLOCKED' THEN 'BLOCKED'
  WHEN 'DONE' THEN 'DONE'
  ELSE UPPER(status)
END
WHERE status IS NOT NULL;

INSERT INTO kanban_column_policies (project_id, status, wip_limit, policy_text)
SELECT p.id,
       s.status,
       s.wip_limit,
       s.policy_text
FROM projects p
CROSS JOIN (
  VALUES
    ('BACKLOG', 18, 'Capture ideas and triage before refinement.'),
    ('READY', 12, 'Refined and ready for commitment.'),
    ('IN_PROGRESS', 6, 'Limit active work and keep focus tight.'),
    ('IN_REVIEW', 4, 'Validate quality before release.'),
    ('BLOCKED', 3, 'Escalate blockers within 24h.'),
    ('DISCARDED', 999, 'Archive items that will not be delivered.'),
    ('DONE', 999, 'Verify outcomes and close the loop.')
) AS s(status, wip_limit, policy_text)
ON CONFLICT (project_id, status) DO NOTHING;
