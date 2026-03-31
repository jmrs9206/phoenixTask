CREATE TABLE issue_activity_events (
  id BIGSERIAL PRIMARY KEY,
  issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
  actor_user_id BIGINT NOT NULL REFERENCES users(id),
  event_type VARCHAR(64) NOT NULL,
  metadata JSONB,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_issue_activity_issue_id ON issue_activity_events(issue_id);
CREATE INDEX idx_issue_activity_created_at ON issue_activity_events(created_at);
