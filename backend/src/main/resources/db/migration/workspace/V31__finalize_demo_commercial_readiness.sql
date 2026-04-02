-- Finalize Demo Commercial Readiness
-- Ensures all demo roles have the required UI permissions (especially permissions.view for the login handshake)
-- and resets credentials to the standard demo password.

-- 1. Ensure permissions.view is assigned to all demo roles if missing
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code = 'permissions.view'
WHERE c.code = 'phoenixtask-demo'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 2. Add essential UI permissions for Elena (QUALITY_ASSURANCE)
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'company.view',
  'users.view',
  'projects.view'
)
WHERE c.code = 'phoenixtask-demo' AND r.code = 'QUALITY_ASSURANCE'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Add essential UI permissions for Pablo (SUPPORT)
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'company.view',
  'users.view',
  'projects.view'
)
WHERE c.code = 'phoenixtask-demo' AND r.code = 'SUPPORT'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 4. Reset/Synchronize demo user credentials to 'PhoenixTask2026!'
UPDATE users
SET password_hash = '$2a$12$nJk1D3Cct1eHobNT0A89PON6G1Cleq52vcC2az4kgouthP/EppSyi'
WHERE email LIKE '%@phoenixtask.demo';
