-- Ensure integrations are not marked CONNECTED without external validation
UPDATE git_integrations
SET status = 'CONFIGURED', updated_at = NOW()
WHERE status = 'CONNECTED';
