ALTER TABLE okr_objectives
  ADD COLUMN scope_type VARCHAR(16) NOT NULL DEFAULT 'COMPANY',
  ADD COLUMN team_id BIGINT NULL REFERENCES teams(id),
  ADD COLUMN confidence_level VARCHAR(16),
  ADD COLUMN final_score NUMERIC(5,2),
  ADD COLUMN closed_at TIMESTAMP;

ALTER TABLE okr_objectives
  ADD CONSTRAINT chk_okr_objective_scope
    CHECK (scope_type IN ('COMPANY', 'TEAM'));

ALTER TABLE okr_objectives
  ADD CONSTRAINT chk_okr_objective_confidence
    CHECK (confidence_level IS NULL OR confidence_level IN ('LOW', 'MEDIUM', 'HIGH'));

ALTER TABLE okr_objectives
  ADD CONSTRAINT chk_okr_objective_score
    CHECK (final_score IS NULL OR (final_score >= 0 AND final_score <= 100));

CREATE TABLE okr_checkins (
  id BIGSERIAL PRIMARY KEY,
  objective_id BIGINT NOT NULL REFERENCES okr_objectives(id) ON DELETE CASCADE,
  author_user_id BIGINT NOT NULL REFERENCES users(id),
  progress_percent NUMERIC(5,2),
  confidence_level VARCHAR(16) NOT NULL,
  note VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE okr_checkins
  ADD CONSTRAINT chk_okr_checkin_confidence
    CHECK (confidence_level IN ('LOW', 'MEDIUM', 'HIGH'));

ALTER TABLE okr_checkins
  ADD CONSTRAINT chk_okr_checkin_progress
    CHECK (progress_percent IS NULL OR (progress_percent >= 0 AND progress_percent <= 100));

CREATE TABLE okr_initiatives (
  id BIGSERIAL PRIMARY KEY,
  objective_id BIGINT NOT NULL REFERENCES okr_objectives(id) ON DELETE CASCADE,
  project_id BIGINT REFERENCES projects(id),
  issue_id BIGINT REFERENCES issues(id),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  CONSTRAINT chk_okr_initiative_target CHECK (project_id IS NOT NULL OR issue_id IS NOT NULL),
  UNIQUE (objective_id, project_id, issue_id)
);
