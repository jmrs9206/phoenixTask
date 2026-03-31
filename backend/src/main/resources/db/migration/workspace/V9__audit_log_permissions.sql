-- Permissions for audit/access logs
INSERT INTO permissions (code, name, description, created_at)
SELECT * FROM (
  VALUES
    ('audit.logs.view', 'Audit Logs: View', 'View audit logs', NOW()),
    ('access.logs.view', 'Access Logs: View', 'View access logs', NOW())
) AS data(code, name, description, created_at)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = data.code);

-- Grant log permissions to OWNER only (admin role)
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'audit.logs.view',
  'access.logs.view'
)
WHERE c.code = 'phoenixtask-demo'
  AND r.code = 'OWNER'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
