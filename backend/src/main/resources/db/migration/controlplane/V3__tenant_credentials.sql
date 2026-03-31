ALTER TABLE tenant_registry
  ADD COLUMN credential_mode VARCHAR(16) NOT NULL DEFAULT 'SHARED';

UPDATE tenant_registry
SET credential_mode = 'SHARED'
WHERE credential_mode IS NULL;
