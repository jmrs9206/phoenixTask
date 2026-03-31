-- Permission for managing public API keys
INSERT INTO permissions (code, name, description, created_at)
SELECT * FROM (
  VALUES
    ('public.api.keys.manage', 'Public API Keys: Manage', 'Create and revoke public API keys', NOW())
) AS data(code, name, description, created_at)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = data.code);

-- Grant public API key management to OWNER only
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN ('public.api.keys.manage')
WHERE c.code = 'phoenixtask-demo'
  AND r.code = 'OWNER'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
