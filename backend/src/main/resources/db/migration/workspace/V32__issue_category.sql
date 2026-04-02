ALTER TABLE issues
  ADD COLUMN category VARCHAR(32) NOT NULL DEFAULT 'GENERAL';

CREATE INDEX idx_issues_category ON issues(category);
