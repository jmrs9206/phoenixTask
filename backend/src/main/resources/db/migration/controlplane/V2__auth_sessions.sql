CREATE TABLE auth_sessions (
  id BIGSERIAL PRIMARY KEY,
  token_hash VARCHAR(64) NOT NULL UNIQUE,
  tenant_code VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  revoked_at TIMESTAMP NULL
);

CREATE INDEX idx_auth_sessions_tenant_code ON auth_sessions (tenant_code);
CREATE INDEX idx_auth_sessions_expires_at ON auth_sessions (expires_at);

CREATE TABLE auth_audit_events (
  id BIGSERIAL PRIMARY KEY,
  tenant_code VARCHAR(64) NULL,
  user_id BIGINT NULL,
  event_type VARCHAR(32) NOT NULL,
  ip_address VARCHAR(64) NULL,
  user_agent VARCHAR(255) NULL,
  detail VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_auth_audit_events_tenant_code ON auth_audit_events (tenant_code);
CREATE INDEX idx_auth_audit_events_created_at ON auth_audit_events (created_at);
