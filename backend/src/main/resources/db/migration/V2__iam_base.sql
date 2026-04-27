ALTER TABLE users
    DROP COLUMN is_active,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER password_hash,
    ADD COLUMN is_platform_internal BOOLEAN NOT NULL DEFAULT FALSE AFTER status,
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE AFTER is_platform_internal,
    ADD COLUMN activated_at TIMESTAMP NULL AFTER updated_at;

CREATE TABLE user_invitations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    invited_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invitations_invited_by FOREIGN KEY (invited_by_user_id) REFERENCES users (id),
    CONSTRAINT uq_invitations_token UNIQUE (token_hash)
) ENGINE=InnoDB;

CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_reset_tokens_token UNIQUE (token_hash)
) ENGINE=InnoDB;

INSERT INTO roles (code, name, description) VALUES
('platform_owner', 'Platform Owner', 'Internal owner of the platform'),
('manager', 'Manager', 'Client manager'),
('developer', 'Developer', 'Client developer'),
('qa', 'QA', 'Client QA'),
('viewer', 'Viewer', 'Read-only viewer');

INSERT INTO users (email, display_name, password_hash, status, is_platform_internal) VALUES
('owner@phoenixtask.com', 'Platform Owner', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ACTIVE', TRUE),
('manager@test.com', 'Test Manager', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ACTIVE', FALSE),
('dev@test.com', 'Test Dev', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ACTIVE', FALSE);

INSERT INTO user_roles (user_id, role_id) VALUES
((SELECT id FROM users WHERE email='owner@phoenixtask.com'), (SELECT id FROM roles WHERE code='platform_owner')),
((SELECT id FROM users WHERE email='manager@test.com'), (SELECT id FROM roles WHERE code='manager')),
((SELECT id FROM users WHERE email='dev@test.com'), (SELECT id FROM roles WHERE code='developer'));
