CREATE TABLE public_api_keys (
  id BIGSERIAL PRIMARY KEY,
  tenant_code VARCHAR(64) NOT NULL,
  label VARCHAR(120) NOT NULL,
  key_prefix VARCHAR(16) NOT NULL,
  key_hash VARCHAR(64) NOT NULL,
  scopes TEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  revoked_at TIMESTAMP NULL
);

CREATE UNIQUE INDEX public_api_keys_hash_idx ON public_api_keys (key_hash);
CREATE INDEX public_api_keys_tenant_idx ON public_api_keys (tenant_code);
