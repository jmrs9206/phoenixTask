-- Restrict analytics access to leadership roles only (remove from SUPPORT/QA).
DELETE FROM role_permissions rp
USING roles r, company c, permissions p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND c.id = r.company_id
  AND c.code = 'phoenixtask-demo'
  AND p.code = 'analytics.view'
  AND r.code IN ('SUPPORT', 'QUALITY_ASSURANCE');
