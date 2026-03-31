CREATE TABLE IF NOT EXISTS controlplane_admin_keys (
  id BIGSERIAL PRIMARY KEY,
  label VARCHAR(120) NOT NULL,
  key_prefix VARCHAR(24) NOT NULL,
  key_hash VARCHAR(128) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  revoked_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_controlplane_admin_keys_hash
  ON controlplane_admin_keys (key_hash);
