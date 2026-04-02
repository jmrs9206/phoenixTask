-- Expand demo data for a more commercial-ready experience

-- Additional PTW issues
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
       'PTW-3',
       'Mobile navigation polish',
       'Refine mobile navigation and compact filters for demo readiness.',
       'IN_PROGRESS',
       'HIGH',
       r.id,
       a.id,
       TIMESTAMP '2026-03-05 09:10',
       TIMESTAMP '2026-03-11 16:30'
FROM projects p
JOIN users r ON r.email = 'sofia.ramos@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'lucia.vega@phoenixtask.demo'
WHERE p.project_key = 'PTW'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'PTW-3');

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
       'PTW-4',
       'Empty state refresh',
       'Update empty states copy and visual balance for core screens.',
       'DONE',
       'MEDIUM',
       r.id,
       a.id,
       TIMESTAMP '2026-03-02 10:00',
       TIMESTAMP '2026-03-09 18:05'
FROM projects p
JOIN users r ON r.email = 'mateo.cruz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'elena.torres@phoenixtask.demo'
WHERE p.project_key = 'PTW'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'PTW-4');

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
       'PTW-5',
       'Insights KPI copy review',
       'Review KPI descriptions to align with data definitions.',
       'OPEN',
       'MEDIUM',
       r.id,
       a.id,
       TIMESTAMP '2026-03-08 09:40',
       TIMESTAMP '2026-03-08 09:40'
FROM projects p
JOIN users r ON r.email = 'sofia.ramos@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'mateo.cruz@phoenixtask.demo'
WHERE p.project_key = 'PTW'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'PTW-5');

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
       'PTW-6',
       'Accessibility sweep for forms',
       'Ensure form labels and focus states meet accessibility expectations.',
       'BLOCKED',
       'MEDIUM',
       r.id,
       a.id,
       TIMESTAMP '2026-03-09 14:20',
       TIMESTAMP '2026-03-10 09:15'
FROM projects p
JOIN users r ON r.email = 'elena.torres@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'lucia.vega@phoenixtask.demo'
WHERE p.project_key = 'PTW'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'PTW-6');

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
       'PTW-7',
       'Demo tour checklist',
       'Curate a crisp demo walkthrough for key modules.',
       'DONE',
       'LOW',
       r.id,
       a.id,
       TIMESTAMP '2026-03-01 12:00',
       TIMESTAMP '2026-03-07 17:45'
FROM projects p
JOIN users r ON r.email = 'mateo.cruz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'sofia.ramos@phoenixtask.demo'
WHERE p.project_key = 'PTW'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'PTW-7');

-- Additional CORE issues
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
  updated_at,
  is_tech_debt
)
SELECT p.id,
       'CORE-2',
       'Public API rate limiting tuning',
       'Tune rate limits and signals for external API usage.',
       'IN_PROGRESS',
       'HIGH',
       r.id,
       a.id,
       TIMESTAMP '2026-03-04 11:15',
       TIMESTAMP '2026-03-12 15:05',
       false
FROM projects p
JOIN users r ON r.email = 'mateo.cruz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'lucia.vega@phoenixtask.demo'
WHERE p.project_key = 'CORE'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'CORE-2');

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
  updated_at,
  is_tech_debt
)
SELECT p.id,
       'CORE-3',
       'Audit log retention policy',
       'Define retention policy and archive strategy for audit logs.',
       'OPEN',
       'MEDIUM',
       r.id,
       a.id,
       TIMESTAMP '2026-03-06 13:00',
       TIMESTAMP '2026-03-06 13:00',
       true
FROM projects p
JOIN users r ON r.email = 'elena.torres@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'elena.torres@phoenixtask.demo'
WHERE p.project_key = 'CORE'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'CORE-3');

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
       'CORE-4',
       'Git webhook resilience pass',
       'Improve retry semantics and signature error handling.',
       'IN_PROGRESS',
       'HIGH',
       r.id,
       a.id,
       TIMESTAMP '2026-03-07 10:30',
       TIMESTAMP '2026-03-15 10:20'
FROM projects p
JOIN users r ON r.email = 'mateo.cruz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'lucia.vega@phoenixtask.demo'
WHERE p.project_key = 'CORE'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'CORE-4');

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
       'CORE-5',
       'Tenant lifecycle guardrails',
       'Refine lifecycle policies to avoid operational dead-ends.',
       'DONE',
       'MEDIUM',
       r.id,
       a.id,
       TIMESTAMP '2026-03-01 09:00',
       TIMESTAMP '2026-03-08 17:10'
FROM projects p
JOIN users r ON r.email = 'sofia.ramos@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'mateo.cruz@phoenixtask.demo'
WHERE p.project_key = 'CORE'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'CORE-5');

-- Additional CSOPS issues
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
       'CSOPS-2',
       'Onboarding playbook update',
       'Refresh onboarding playbook with latest product updates.',
       'IN_PROGRESS',
       'MEDIUM',
       r.id,
       a.id,
       TIMESTAMP '2026-03-05 15:30',
       TIMESTAMP '2026-03-12 12:00'
FROM projects p
JOIN users r ON r.email = 'pablo.ruiz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'pablo.ruiz@phoenixtask.demo'
WHERE p.project_key = 'CSOPS'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'CSOPS-2');

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
       'CSOPS-3',
       'Customer feedback triage board',
       'Set up a lightweight triage board for inbound feedback.',
       'OPEN',
       'LOW',
       r.id,
       a.id,
       TIMESTAMP '2026-03-10 10:00',
       TIMESTAMP '2026-03-10 10:00'
FROM projects p
JOIN users r ON r.email = 'pablo.ruiz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'pablo.ruiz@phoenixtask.demo'
WHERE p.project_key = 'CSOPS'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'CSOPS-3');

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
       'CSOPS-4',
       'Quarterly health review deck',
       'Draft the quarterly health review deck for CS leadership.',
       'DONE',
       'LOW',
       r.id,
       a.id,
       TIMESTAMP '2026-03-02 09:45',
       TIMESTAMP '2026-03-06 16:40'
FROM projects p
JOIN users r ON r.email = 'pablo.ruiz@phoenixtask.demo'
LEFT JOIN users a ON a.email = 'sofia.ramos@phoenixtask.demo'
WHERE p.project_key = 'CSOPS'
  AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_key = 'CSOPS-4');

-- Planned dates and sprint assignment
UPDATE issues
SET planned_start_date = DATE '2026-03-08',
    due_date = DATE '2026-03-14'
WHERE issue_key = 'PTW-3';

UPDATE issues
SET planned_start_date = DATE '2026-03-02',
    due_date = DATE '2026-03-09'
WHERE issue_key = 'PTW-4';

UPDATE issues
SET planned_start_date = DATE '2026-03-08',
    due_date = DATE '2026-03-18'
WHERE issue_key = 'PTW-5';

UPDATE issues
SET planned_start_date = DATE '2026-03-09',
    due_date = DATE '2026-03-20'
WHERE issue_key = 'PTW-6';

UPDATE issues
SET planned_start_date = DATE '2026-03-01',
    due_date = DATE '2026-03-07'
WHERE issue_key = 'PTW-7';

UPDATE issues
SET planned_start_date = DATE '2026-03-04',
    due_date = DATE '2026-03-18'
WHERE issue_key = 'CORE-2';

UPDATE issues
SET planned_start_date = DATE '2026-03-06',
    due_date = DATE '2026-03-28'
WHERE issue_key = 'CORE-3';

UPDATE issues
SET planned_start_date = DATE '2026-03-07',
    due_date = DATE '2026-03-22'
WHERE issue_key = 'CORE-4';

UPDATE issues
SET planned_start_date = DATE '2026-03-01',
    due_date = DATE '2026-03-10'
WHERE issue_key = 'CORE-5';

UPDATE issues
SET planned_start_date = DATE '2026-03-05',
    due_date = DATE '2026-03-19'
WHERE issue_key = 'CSOPS-2';

UPDATE issues
SET planned_start_date = DATE '2026-03-10',
    due_date = DATE '2026-03-24'
WHERE issue_key = 'CSOPS-3';

UPDATE issues
SET planned_start_date = DATE '2026-03-02',
    due_date = DATE '2026-03-06'
WHERE issue_key = 'CSOPS-4';

UPDATE issues
SET sprint_id = (
  SELECT s.id
  FROM sprints s
  JOIN projects p ON p.id = s.project_id
  WHERE p.project_key = 'PTW' AND s.name = 'Sprint 1'
)
WHERE issue_key IN ('PTW-1', 'PTW-2', 'PTW-3', 'PTW-4');

-- Issue comments
INSERT INTO issue_comments (issue_id, author_user_id, body, created_at, updated_at)
SELECT i.id, u.id, 'Revisemos este flujo con enfoque demo y checklist actualizada.', NOW(), NOW()
FROM issues i
JOIN users u ON u.email = 'sofia.ramos@phoenixtask.demo'
WHERE i.issue_key = 'PTW-1'
  AND NOT EXISTS (
    SELECT 1 FROM issue_comments c WHERE c.issue_id = i.id AND c.body LIKE 'Revisemos este flujo%'
  );

INSERT INTO issue_comments (issue_id, author_user_id, body, created_at, updated_at)
SELECT i.id, u.id, 'Estoy ajustando los textos de settings para el walkthrough.', NOW(), NOW()
FROM issues i
JOIN users u ON u.email = 'lucia.vega@phoenixtask.demo'
WHERE i.issue_key = 'PTW-2'
  AND NOT EXISTS (
    SELECT 1 FROM issue_comments c WHERE c.issue_id = i.id AND c.body LIKE 'Estoy ajustando%'
  );

INSERT INTO issue_comments (issue_id, author_user_id, body, created_at, updated_at)
SELECT i.id, u.id, 'Necesitamos validar con QA antes de cerrar.', NOW(), NOW()
FROM issues i
JOIN users u ON u.email = 'elena.torres@phoenixtask.demo'
WHERE i.issue_key = 'PTW-4'
  AND NOT EXISTS (
    SELECT 1 FROM issue_comments c WHERE c.issue_id = i.id AND c.body LIKE 'Necesitamos validar%'
  );

INSERT INTO issue_comments (issue_id, author_user_id, body, created_at, updated_at)
SELECT i.id, u.id, 'Pendiente de datos de uso real para definir limites.', NOW(), NOW()
FROM issues i
JOIN users u ON u.email = 'mateo.cruz@phoenixtask.demo'
WHERE i.issue_key = 'CORE-2'
  AND NOT EXISTS (
    SELECT 1 FROM issue_comments c WHERE c.issue_id = i.id AND c.body LIKE 'Pendiente de datos%'
  );

INSERT INTO issue_comments (issue_id, author_user_id, body, created_at, updated_at)
SELECT i.id, u.id, 'Sin hallazgos criticos; listo para seguimiento mensual.', NOW(), NOW()
FROM issues i
JOIN users u ON u.email = 'pablo.ruiz@phoenixtask.demo'
WHERE i.issue_key = 'CSOPS-4'
  AND NOT EXISTS (
    SELECT 1 FROM issue_comments c WHERE c.issue_id = i.id AND c.body LIKE 'Sin hallazgos%'
  );

-- Issue attachments (files created by dev demo assets script)
INSERT INTO issue_attachments (issue_id, uploader_user_id, original_filename, stored_filename, mime_type, size_bytes, created_at)
SELECT i.id, u.id, 'Onboarding-Checklist.pdf', 'demo-onboarding-checklist.pdf', 'application/pdf', 20480, NOW()
FROM issues i
JOIN users u ON u.email = 'sofia.ramos@phoenixtask.demo'
WHERE i.issue_key = 'PTW-1'
  AND NOT EXISTS (
    SELECT 1 FROM issue_attachments a WHERE a.stored_filename = 'demo-onboarding-checklist.pdf'
  );

INSERT INTO issue_attachments (issue_id, uploader_user_id, original_filename, stored_filename, mime_type, size_bytes, created_at)
SELECT i.id, u.id, 'CS-Health-Review.txt', 'demo-cs-health-review.txt', 'text/plain', 2048, NOW()
FROM issues i
JOIN users u ON u.email = 'pablo.ruiz@phoenixtask.demo'
WHERE i.issue_key = 'CSOPS-4'
  AND NOT EXISTS (
    SELECT 1 FROM issue_attachments a WHERE a.stored_filename = 'demo-cs-health-review.txt'
  );

INSERT INTO issue_attachments (issue_id, uploader_user_id, original_filename, stored_filename, mime_type, size_bytes, created_at)
SELECT i.id, u.id, 'UI-Preview.png', 'demo-ui-preview.png', 'image/png', 1024, NOW()
FROM issues i
JOIN users u ON u.email = 'lucia.vega@phoenixtask.demo'
WHERE i.issue_key = 'PTW-2'
  AND NOT EXISTS (
    SELECT 1 FROM issue_attachments a WHERE a.stored_filename = 'demo-ui-preview.png'
  );

-- Issue activity events
INSERT INTO issue_activity_events (issue_id, actor_user_id, event_type, metadata, created_at)
SELECT i.id, i.reporter_user_id, 'ISSUE_CREATED', NULL, i.created_at
FROM issues i
WHERE i.issue_key IN ('PTW-3', 'PTW-4', 'PTW-5', 'PTW-6', 'PTW-7', 'CORE-2', 'CORE-3', 'CORE-4', 'CORE-5', 'CSOPS-2', 'CSOPS-3', 'CSOPS-4')
  AND NOT EXISTS (
    SELECT 1 FROM issue_activity_events e WHERE e.issue_id = i.id AND e.event_type = 'ISSUE_CREATED'
  );

INSERT INTO issue_activity_events (issue_id, actor_user_id, event_type, metadata, created_at)
SELECT i.id, c.author_user_id, 'COMMENT_CREATED', NULL, c.created_at
FROM issue_comments c
JOIN issues i ON i.id = c.issue_id
WHERE NOT EXISTS (
  SELECT 1 FROM issue_activity_events e
  WHERE e.issue_id = i.id AND e.event_type = 'COMMENT_CREATED' AND e.created_at = c.created_at
);

INSERT INTO issue_activity_events (issue_id, actor_user_id, event_type, metadata, created_at)
SELECT i.id, a.uploader_user_id, 'ATTACHMENT_UPLOADED', NULL, a.created_at
FROM issue_attachments a
JOIN issues i ON i.id = a.issue_id
WHERE NOT EXISTS (
  SELECT 1 FROM issue_activity_events e
  WHERE e.issue_id = i.id AND e.event_type = 'ATTACHMENT_UPLOADED' AND e.created_at = a.created_at
);

-- Scrum snapshots + burndown
INSERT INTO sprint_commitments (sprint_id, issue_id, captured_at)
SELECT s.id, i.id, TIMESTAMP '2026-03-03 09:05'
FROM sprints s
JOIN issues i ON i.issue_key IN ('PTW-1', 'PTW-2', 'PTW-3', 'PTW-4')
WHERE s.name = 'Sprint 1'
  AND NOT EXISTS (
    SELECT 1 FROM sprint_commitments sc WHERE sc.sprint_id = s.id AND sc.issue_id = i.id
  );

INSERT INTO sprint_commitment_snapshots (sprint_id, captured_at, committed_count)
SELECT s.id, TIMESTAMP '2026-03-03 09:10', 4
FROM sprints s
WHERE s.name = 'Sprint 1'
ON CONFLICT (sprint_id) DO UPDATE
SET committed_count = EXCLUDED.committed_count,
    captured_at = EXCLUDED.captured_at;

INSERT INTO sprint_burndown_points (sprint_id, point_date, committed_count, remaining_count, created_at)
SELECT s.id, v.point_date, 4, v.remaining_count, NOW()
FROM sprints s
JOIN (
  VALUES
    (DATE '2026-03-03', 4),
    (DATE '2026-03-06', 3),
    (DATE '2026-03-10', 2),
    (DATE '2026-03-14', 1),
    (DATE '2026-03-17', 0)
) AS v(point_date, remaining_count)
ON TRUE
WHERE s.name = 'Sprint 1'
ON CONFLICT (sprint_id, point_date) DO NOTHING;

-- OKR updates and expansion
UPDATE okr_objectives
SET status = 'COMPLETED',
    confidence_level = 'MEDIUM',
    final_score = 78.00,
    closed_at = TIMESTAMP '2026-03-28 16:30',
    updated_at = NOW()
WHERE title IN ('Increase onboarding completion', 'ok');

INSERT INTO okr_objectives (
  company_id,
  project_id,
  owner_user_id,
  title,
  description,
  status,
  period_start,
  period_end,
  confidence_level,
  created_at,
  updated_at
)
SELECT c.id,
       p.id,
       u.id,
       'Stabilize platform readiness',
       'Reduce incidents and improve delivery reliability.',
       'ACTIVE',
       DATE '2026-03-01',
       DATE '2026-06-30',
       'HIGH',
       NOW(),
       NOW()
FROM company c
JOIN projects p ON p.project_key = 'CORE'
JOIN users u ON u.email = 'mateo.cruz@phoenixtask.demo'
WHERE c.code = 'phoenixtask-demo'
  AND NOT EXISTS (
    SELECT 1 FROM okr_objectives o WHERE o.title = 'Stabilize platform readiness'
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
       o.project_id,
       'Automation coverage to 85%',
       85,
       64,
       'percent',
       'AT_RISK',
       NOW(),
       NOW()
FROM okr_objectives o
WHERE o.title = 'Stabilize platform readiness'
  AND NOT EXISTS (
    SELECT 1 FROM okr_key_results kr WHERE kr.title = 'Automation coverage to 85%'
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
       o.project_id,
       'Webhook delivery success 98%',
       98,
       91,
       'percent',
       'ON_TRACK',
       NOW(),
       NOW()
FROM okr_objectives o
WHERE o.title = 'Stabilize platform readiness'
  AND NOT EXISTS (
    SELECT 1 FROM okr_key_results kr WHERE kr.title = 'Webhook delivery success 98%'
  );

INSERT INTO okr_checkins (
  objective_id,
  author_user_id,
  progress_percent,
  confidence_level,
  note,
  created_at
)
SELECT o.id,
       u.id,
       52,
       'MEDIUM',
       'Onboarding improvements rolled out to beta users.',
       NOW()
FROM okr_objectives o
JOIN users u ON u.email = 'sofia.ramos@phoenixtask.demo'
WHERE o.title = 'Increase onboarding completion'
  AND NOT EXISTS (
    SELECT 1 FROM okr_checkins c WHERE c.objective_id = o.id AND c.note LIKE 'Onboarding improvements%'
  );

INSERT INTO okr_checkins (
  objective_id,
  author_user_id,
  progress_percent,
  confidence_level,
  note,
  created_at
)
SELECT o.id,
       u.id,
       61,
       'HIGH',
       'Platform readiness KPIs trending upward after infra tuning.',
       NOW()
FROM okr_objectives o
JOIN users u ON u.email = 'mateo.cruz@phoenixtask.demo'
WHERE o.title = 'Stabilize platform readiness'
  AND NOT EXISTS (
    SELECT 1 FROM okr_checkins c WHERE c.objective_id = o.id AND c.note LIKE 'Platform readiness KPIs%'
  );

INSERT INTO okr_initiatives (objective_id, project_id, issue_id, created_at)
SELECT o.id, o.project_id, i.id, NOW()
FROM okr_objectives o
JOIN issues i ON i.issue_key = 'CORE-4'
WHERE o.title = 'Stabilize platform readiness'
  AND NOT EXISTS (
    SELECT 1 FROM okr_initiatives oi WHERE oi.objective_id = o.id AND oi.issue_id = i.id
  );

-- Git demo integration + repository + code links
INSERT INTO git_integrations (
  company_id,
  provider,
  label,
  token_hash,
  token_prefix,
  webhook_secret,
  status,
  created_at,
  updated_at
)
SELECT c.id,
       'GITHUB',
       'demo-github',
       'demo_hash_0f4a7b2e8d4c1b9f0f4a7b2e8d4c1b9f0f4a7b2e8d4c1b9f',
       'ghp_demo',
       'demo-webhook-secret',
       'CONFIGURED',
       NOW(),
       NOW()
FROM company c
WHERE c.code = 'phoenixtask-demo'
  AND NOT EXISTS (
    SELECT 1 FROM git_integrations gi WHERE gi.label = 'demo-github'
  );

INSERT INTO git_repositories (
  integration_id,
  project_id,
  repo_owner,
  repo_name,
  default_branch,
  status,
  created_at,
  updated_at
)
SELECT gi.id,
       p.id,
       'phoenixtask',
       'phoenixtask-web',
       'main',
       'CONNECTED',
       NOW(),
       NOW()
FROM git_integrations gi
JOIN projects p ON p.project_key = 'PTW'
WHERE gi.label = 'demo-github'
  AND NOT EXISTS (
    SELECT 1 FROM git_repositories gr
    WHERE gr.integration_id = gi.id AND gr.repo_owner = 'phoenixtask' AND gr.repo_name = 'phoenixtask-web'
  );

INSERT INTO git_repositories (
  integration_id,
  project_id,
  repo_owner,
  repo_name,
  default_branch,
  status,
  created_at,
  updated_at
)
SELECT gi.id,
       p.id,
       'phoenixtask',
       'phoenixtask-core',
       'main',
       'CONNECTED',
       NOW(),
       NOW()
FROM git_integrations gi
JOIN projects p ON p.project_key = 'CORE'
WHERE gi.label = 'demo-github'
  AND NOT EXISTS (
    SELECT 1 FROM git_repositories gr
    WHERE gr.integration_id = gi.id AND gr.repo_owner = 'phoenixtask' AND gr.repo_name = 'phoenixtask-core'
  );

INSERT INTO issue_code_links (
  issue_id,
  project_id,
  integration_id,
  repo_id,
  provider,
  artifact_type,
  external_id,
  title,
  url,
  author_name,
  external_created_at,
  created_at,
  updated_at
)
SELECT i.id,
       p.id,
       gi.id,
       gr.id,
       'GITHUB',
       'BRANCH',
       'PTW-2-onboarding',
       'PTW-2-onboarding',
       'https://github.com/phoenixtask/phoenixtask-web/tree/PTW-2-onboarding',
       'Lucia Vega',
       TIMESTAMPTZ '2026-03-06 09:30+00',
       NOW(),
       NOW()
FROM issues i
JOIN projects p ON p.id = i.project_id
JOIN git_integrations gi ON gi.label = 'demo-github'
JOIN git_repositories gr ON gr.integration_id = gi.id AND gr.project_id = p.id
WHERE i.issue_key = 'PTW-2'
  AND NOT EXISTS (
    SELECT 1 FROM issue_code_links l
    WHERE l.issue_id = i.id AND l.artifact_type = 'BRANCH' AND l.external_id = 'PTW-2-onboarding'
  );

INSERT INTO issue_code_links (
  issue_id,
  project_id,
  integration_id,
  repo_id,
  provider,
  artifact_type,
  external_id,
  title,
  url,
  author_name,
  external_created_at,
  created_at,
  updated_at
)
SELECT i.id,
       p.id,
       gi.id,
       gr.id,
       'GITHUB',
       'COMMIT',
       'a1b2c3d4',
       'Improve settings quick-start',
       'https://github.com/phoenixtask/phoenixtask-web/commit/a1b2c3d4',
       'Lucia Vega',
       TIMESTAMPTZ '2026-03-08 12:10+00',
       NOW(),
       NOW()
FROM issues i
JOIN projects p ON p.id = i.project_id
JOIN git_integrations gi ON gi.label = 'demo-github'
JOIN git_repositories gr ON gr.integration_id = gi.id AND gr.project_id = p.id
WHERE i.issue_key = 'PTW-2'
  AND NOT EXISTS (
    SELECT 1 FROM issue_code_links l
    WHERE l.issue_id = i.id AND l.artifact_type = 'COMMIT' AND l.external_id = 'a1b2c3d4'
  );

INSERT INTO issue_code_links (
  issue_id,
  project_id,
  integration_id,
  repo_id,
  provider,
  artifact_type,
  external_id,
  title,
  url,
  author_name,
  external_created_at,
  created_at,
  updated_at
)
SELECT i.id,
       p.id,
       gi.id,
       gr.id,
       'GITHUB',
       'PULL_REQUEST',
       '42',
       'PTW-2: Settings quick-start',
       'https://github.com/phoenixtask/phoenixtask-web/pull/42',
       'Lucia Vega',
       TIMESTAMPTZ '2026-03-09 15:00+00',
       NOW(),
       NOW()
FROM issues i
JOIN projects p ON p.id = i.project_id
JOIN git_integrations gi ON gi.label = 'demo-github'
JOIN git_repositories gr ON gr.integration_id = gi.id AND gr.project_id = p.id
WHERE i.issue_key = 'PTW-2'
  AND NOT EXISTS (
    SELECT 1 FROM issue_code_links l
    WHERE l.issue_id = i.id AND l.artifact_type = 'PULL_REQUEST' AND l.external_id = '42'
  );

INSERT INTO issue_code_links (
  issue_id,
  project_id,
  integration_id,
  repo_id,
  provider,
  artifact_type,
  external_id,
  title,
  url,
  author_name,
  external_created_at,
  created_at,
  updated_at
)
SELECT i.id,
       p.id,
       gi.id,
       gr.id,
       'GITHUB',
       'BRANCH',
       'CORE-2-rate-limits',
       'CORE-2-rate-limits',
       'https://github.com/phoenixtask/phoenixtask-core/tree/CORE-2-rate-limits',
       'Mateo Cruz',
       TIMESTAMPTZ '2026-03-07 09:00+00',
       NOW(),
       NOW()
FROM issues i
JOIN projects p ON p.id = i.project_id
JOIN git_integrations gi ON gi.label = 'demo-github'
JOIN git_repositories gr ON gr.integration_id = gi.id AND gr.project_id = p.id
WHERE i.issue_key = 'CORE-2'
  AND NOT EXISTS (
    SELECT 1 FROM issue_code_links l
    WHERE l.issue_id = i.id AND l.artifact_type = 'BRANCH' AND l.external_id = 'CORE-2-rate-limits'
  );

-- Update project issue counters
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
