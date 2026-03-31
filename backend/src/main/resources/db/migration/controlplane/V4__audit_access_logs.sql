CREATE TABLE audit_events (
  id BIGSERIAL PRIMARY KEY,
  tenant_code VARCHAR(64),
  domain VARCHAR(48) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  actor_type VARCHAR(32),
  actor_id BIGINT,
  actor_email VARCHAR(255),
  resource_type VARCHAR(64),
  resource_id VARCHAR(128),
  outcome VARCHAR(32),
  ip_address VARCHAR(64),
  user_agent VARCHAR(255),
  request_id VARCHAR(64),
  detail VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_events_tenant ON audit_events(tenant_code);
CREATE INDEX idx_audit_events_domain ON audit_events(domain);
CREATE INDEX idx_audit_events_event_type ON audit_events(event_type);
CREATE INDEX idx_audit_events_created_at ON audit_events(created_at);

CREATE TABLE access_logs (
  id BIGSERIAL PRIMARY KEY,
  request_id VARCHAR(64),
  tenant_code VARCHAR(64),
  user_id BIGINT,
  user_email VARCHAR(255),
  http_method VARCHAR(16),
  path VARCHAR(255),
  status INTEGER,
  ip_address VARCHAR(64),
  user_agent VARCHAR(255),
  duration_ms BIGINT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_access_logs_tenant ON access_logs(tenant_code);
CREATE INDEX idx_access_logs_path ON access_logs(path);
CREATE INDEX idx_access_logs_status ON access_logs(status);
CREATE INDEX idx_access_logs_created_at ON access_logs(created_at);
