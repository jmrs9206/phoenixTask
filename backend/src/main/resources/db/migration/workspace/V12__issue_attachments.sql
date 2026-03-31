CREATE TABLE issue_attachments (
  id BIGSERIAL PRIMARY KEY,
  issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
  uploader_user_id BIGINT NOT NULL REFERENCES users(id),
  original_filename VARCHAR(255) NOT NULL,
  stored_filename VARCHAR(255) NOT NULL,
  mime_type VARCHAR(128) NOT NULL,
  size_bytes BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_issue_attachments_issue_id ON issue_attachments(issue_id);
CREATE INDEX idx_issue_attachments_uploader_user_id ON issue_attachments(uploader_user_id);
