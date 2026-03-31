CREATE TABLE issue_comments (
  id BIGSERIAL PRIMARY KEY,
  issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
  author_user_id BIGINT NOT NULL REFERENCES users(id),
  body TEXT NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_issue_comments_issue_id ON issue_comments(issue_id);
CREATE INDEX idx_issue_comments_author_id ON issue_comments(author_user_id);
