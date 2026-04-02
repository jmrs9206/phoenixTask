-- Assign core workspace permissions to demo roles
-- Elena (QUALITY_ASSURANCE) - Read-Only access to almost everything
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'issues.view',
  'issues.comment.view',
  'scrum.sprint.view',
  'scrum.backlog.view',
  'kanban.board.view',
  'okr.objective.view',
  'gantt.view',
  'messages.read'
)
WHERE c.code = 'phoenixtask-demo' AND r.code = 'QUALITY_ASSURANCE'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Pablo (SUPPORT) - Access to limited set only
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'issues.view',
  'issues.comment.view',
  'issues.comment.create',
  'messages.read',
  'messages.send'
)
WHERE c.code = 'phoenixtask-demo' AND r.code = 'SUPPORT'
ON CONFLICT (role_id, permission_id) DO NOTHING;
