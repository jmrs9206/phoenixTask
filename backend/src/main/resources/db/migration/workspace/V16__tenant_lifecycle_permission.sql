-- Permission for managing tenant lifecycle
INSERT INTO permissions (code, name, description, created_at)
SELECT * FROM (
  VALUES
    ('tenant.lifecycle.manage', 'Tenant Lifecycle: Manage', 'Suspend or reactivate tenant', NOW())
) AS data(code, name, description, created_at)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = data.code);

-- Grant tenant lifecycle management to OWNER only
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN ('tenant.lifecycle.manage')
WHERE c.code = 'phoenixtask-demo'
  AND r.code = 'OWNER'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
