-- Extend permissions catalog for new modules (issues, messages, scrum, kanban, okr, gantt, analytics)
INSERT INTO permissions (code, name, description, created_at)
SELECT * FROM (
  VALUES
    ('issues.view', 'Issues: View', 'View issues', NOW()),
    ('issues.create', 'Issues: Create', 'Create issues', NOW()),
    ('issues.update', 'Issues: Update', 'Update issues', NOW()),
    ('issues.assign', 'Issues: Assign', 'Assign issues', NOW()),
    ('issues.transition', 'Issues: Transition', 'Transition issues', NOW()),
    ('issues.comment.view', 'Issues Comments: View', 'View issue comments', NOW()),
    ('issues.comment.create', 'Issues Comments: Create', 'Create issue comments', NOW()),
    ('issues.comment.update', 'Issues Comments: Update', 'Update issue comments', NOW()),
    ('issues.comment.delete', 'Issues Comments: Delete', 'Delete issue comments', NOW()),
    ('issues.attachment.upload', 'Issues Attachments: Upload', 'Upload issue attachments', NOW()),
    ('issues.attachment.delete', 'Issues Attachments: Delete', 'Delete issue attachments', NOW()),
    ('messages.read', 'Messages: Read', 'Read messages', NOW()),
    ('messages.send', 'Messages: Send', 'Send messages', NOW()),
    ('messages.thread.create', 'Messages: Create Thread', 'Create message threads', NOW()),
    ('scrum.sprint.view', 'Scrum: View Sprints', 'View sprints', NOW()),
    ('scrum.sprint.create', 'Scrum: Create Sprints', 'Create sprints', NOW()),
    ('scrum.sprint.update', 'Scrum: Update Sprints', 'Update sprints', NOW()),
    ('scrum.sprint.complete', 'Scrum: Complete Sprints', 'Complete sprints', NOW()),
    ('scrum.backlog.view', 'Scrum: View Backlog', 'View sprint backlog', NOW()),
    ('scrum.backlog.update', 'Scrum: Update Backlog', 'Assign backlog items', NOW()),
    ('kanban.board.view', 'Kanban: View Board', 'View kanban board', NOW()),
    ('kanban.issue.move', 'Kanban: Move Issue', 'Move issues on kanban', NOW()),
    ('okr.objective.view', 'OKR: View Objectives', 'View OKR objectives', NOW()),
    ('okr.objective.create', 'OKR: Create Objectives', 'Create OKR objectives', NOW()),
    ('okr.objective.update', 'OKR: Update Objectives', 'Update OKR objectives', NOW()),
    ('okr.objective.close', 'OKR: Close Objectives', 'Close OKR objectives', NOW()),
    ('okr.key_result.manage', 'OKR: Manage Key Results', 'Manage OKR key results', NOW()),
    ('gantt.view', 'Gantt: View', 'View gantt timeline', NOW()),
    ('gantt.update', 'Gantt: Update', 'Update gantt timeline', NOW()),
    ('analytics.view', 'Analytics: View', 'View analytics', NOW())
) AS data(code, name, description, created_at)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = data.code);

-- Grant new permissions to OWNER
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'issues.view', 'issues.create', 'issues.update', 'issues.assign', 'issues.transition',
  'issues.comment.view', 'issues.comment.create', 'issues.comment.update', 'issues.comment.delete',
  'issues.attachment.upload', 'issues.attachment.delete',
  'messages.read', 'messages.send', 'messages.thread.create',
  'scrum.sprint.view', 'scrum.sprint.create', 'scrum.sprint.update', 'scrum.sprint.complete',
  'scrum.backlog.view', 'scrum.backlog.update',
  'kanban.board.view', 'kanban.issue.move',
  'okr.objective.view', 'okr.objective.create', 'okr.objective.update', 'okr.objective.close',
  'okr.key_result.manage',
  'gantt.view', 'gantt.update',
  'analytics.view'
)
WHERE c.code = 'phoenixtask-demo'
  AND r.code = 'OWNER'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Grant core work permissions to TEAM_LEADER
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'issues.view', 'issues.create', 'issues.update', 'issues.assign', 'issues.transition',
  'issues.comment.view', 'issues.comment.create', 'issues.comment.update',
  'issues.attachment.upload',
  'messages.read', 'messages.send', 'messages.thread.create',
  'scrum.sprint.view', 'scrum.sprint.create', 'scrum.sprint.update',
  'scrum.backlog.view', 'scrum.backlog.update',
  'kanban.board.view', 'kanban.issue.move',
  'okr.objective.view', 'okr.objective.create', 'okr.objective.update',
  'okr.key_result.manage',
  'gantt.view',
  'analytics.view'
)
WHERE c.code = 'phoenixtask-demo'
  AND r.code = 'TEAM_LEADER'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Grant execution permissions to DEVELOPER
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'issues.view', 'issues.update', 'issues.transition',
  'issues.comment.view', 'issues.comment.create',
  'issues.attachment.upload',
  'messages.read', 'messages.send',
  'scrum.sprint.view', 'scrum.backlog.view',
  'kanban.board.view',
  'gantt.view',
  'analytics.view'
)
WHERE c.code = 'phoenixtask-demo'
  AND r.code = 'DEVELOPER'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Grant read permissions to SUPPORT and QA
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'issues.view', 'issues.comment.view',
  'messages.read',
  'scrum.sprint.view', 'scrum.backlog.view',
  'kanban.board.view',
  'okr.objective.view',
  'gantt.view',
  'analytics.view'
)
WHERE c.code = 'phoenixtask-demo'
  AND r.code IN ('SUPPORT', 'QUALITY_ASSURANCE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
