-- Demo messages to avoid empty Threads in demo workspace
INSERT INTO messages (thread_id, author_user_id, body, created_at)
SELECT mt.id, u.id, 'Welcome to the Product team thread. Please share roadmap updates here.', NOW()
FROM message_threads mt
JOIN teams t ON t.id = mt.team_id
JOIN users u ON u.email = 'sofia.ramos@phoenixtask.demo'
WHERE mt.thread_type = 'TEAM' AND t.name = 'Product';

INSERT INTO messages (thread_id, author_user_id, body, created_at)
SELECT mt.id, u.id, 'Engineering sync: platform stability and delivery timelines.', NOW()
FROM message_threads mt
JOIN teams t ON t.id = mt.team_id
JOIN users u ON u.email = 'mateo.cruz@phoenixtask.demo'
WHERE mt.thread_type = 'TEAM' AND t.name = 'Engineering';

INSERT INTO messages (thread_id, author_user_id, body, created_at)
SELECT mt.id, u.id, 'Customer Success updates: escalations and handoff follow-ups.', NOW()
FROM message_threads mt
JOIN teams t ON t.id = mt.team_id
JOIN users u ON u.email = 'elena.torres@phoenixtask.demo'
WHERE mt.thread_type = 'TEAM' AND t.name = 'Customer Success';

INSERT INTO messages (thread_id, author_user_id, body, created_at)
SELECT mt.id, u.id, 'Project thread kickoff: align on PhoenixTask Web milestones.', NOW()
FROM message_threads mt
JOIN projects p ON p.id = mt.project_id
JOIN users u ON u.email = 'mateo.cruz@phoenixtask.demo'
WHERE mt.thread_type = 'PROJECT' AND p.project_key = 'PTW';

INSERT INTO messages (thread_id, author_user_id, body, created_at)
SELECT mt.id, u.id, 'Platform Core: tracking auth + policy engine follow-ups.', NOW()
FROM message_threads mt
JOIN projects p ON p.id = mt.project_id
JOIN users u ON u.email = 'lucia.vega@phoenixtask.demo'
WHERE mt.thread_type = 'PROJECT' AND p.project_key = 'CORE';

INSERT INTO messages (thread_id, author_user_id, body, created_at)
SELECT mt.id, u.id, 'CS Ops: keep onboarding checklist aligned with issues.', NOW()
FROM message_threads mt
JOIN projects p ON p.id = mt.project_id
JOIN users u ON u.email = 'pablo.ruiz@phoenixtask.demo'
WHERE mt.thread_type = 'PROJECT' AND p.project_key = 'CSOPS';

UPDATE message_threads mt
SET last_message_at = sub.max_created
FROM (
  SELECT thread_id, MAX(created_at) AS max_created
  FROM messages
  GROUP BY thread_id
) sub
WHERE mt.id = sub.thread_id AND mt.last_message_at IS NULL;
