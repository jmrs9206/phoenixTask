CREATE TABLE message_threads (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT NOT NULL REFERENCES company(id),
  thread_type VARCHAR(16) NOT NULL,
  team_id BIGINT NULL REFERENCES teams(id),
  project_id BIGINT NULL REFERENCES projects(id),
  direct_user_one_id BIGINT NULL REFERENCES users(id),
  direct_user_two_id BIGINT NULL REFERENCES users(id),
  last_message_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE message_threads
  ADD CONSTRAINT chk_message_threads_type
    CHECK (thread_type IN ('TEAM', 'PROJECT', 'DIRECT'));

ALTER TABLE message_threads
  ADD CONSTRAINT chk_message_threads_team
    CHECK (
      thread_type <> 'TEAM'
      OR (team_id IS NOT NULL AND project_id IS NULL AND direct_user_one_id IS NULL AND direct_user_two_id IS NULL)
    );

ALTER TABLE message_threads
  ADD CONSTRAINT chk_message_threads_project
    CHECK (
      thread_type <> 'PROJECT'
      OR (project_id IS NOT NULL AND team_id IS NULL AND direct_user_one_id IS NULL AND direct_user_two_id IS NULL)
    );

ALTER TABLE message_threads
  ADD CONSTRAINT chk_message_threads_direct
    CHECK (
      thread_type <> 'DIRECT'
      OR (
        direct_user_one_id IS NOT NULL
        AND direct_user_two_id IS NOT NULL
        AND team_id IS NULL
        AND project_id IS NULL
        AND direct_user_one_id < direct_user_two_id
      )
    );

CREATE UNIQUE INDEX uq_message_threads_team
  ON message_threads(company_id, team_id)
  WHERE thread_type = 'TEAM';

CREATE UNIQUE INDEX uq_message_threads_project
  ON message_threads(company_id, project_id)
  WHERE thread_type = 'PROJECT';

CREATE UNIQUE INDEX uq_message_threads_direct
  ON message_threads(company_id, direct_user_one_id, direct_user_two_id)
  WHERE thread_type = 'DIRECT';

CREATE TABLE messages (
  id BIGSERIAL PRIMARY KEY,
  thread_id BIGINT NOT NULL REFERENCES message_threads(id),
  author_user_id BIGINT NOT NULL REFERENCES users(id),
  body VARCHAR(2000) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_thread_created_at ON messages(thread_id, created_at);

INSERT INTO message_threads (company_id, thread_type, team_id, created_at)
SELECT t.company_id, 'TEAM', t.id, NOW()
FROM teams t;

INSERT INTO message_threads (company_id, thread_type, project_id, created_at)
SELECT p.company_id, 'PROJECT', p.id, NOW()
FROM projects p;
