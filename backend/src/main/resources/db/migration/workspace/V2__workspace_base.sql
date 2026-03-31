CREATE TABLE company (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(200) NOT NULL,
  status VARCHAR(32) NOT NULL,
  owner_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE company_settings (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT NOT NULL UNIQUE REFERENCES company(id),
  timezone VARCHAR(64) NOT NULL,
  locale VARCHAR(16) NOT NULL,
  week_start VARCHAR(16) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE roles (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT NOT NULL REFERENCES company(id),
  code VARCHAR(64) NOT NULL,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(255),
  is_internal BOOLEAN NOT NULL,
  is_assignable BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (company_id, code)
);

CREATE TABLE permissions (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(120) NOT NULL UNIQUE,
  name VARCHAR(200) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE role_permissions (
  id BIGSERIAL PRIMARY KEY,
  role_id BIGINT NOT NULL REFERENCES roles(id),
  permission_id BIGINT NOT NULL REFERENCES permissions(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (role_id, permission_id)
);

CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT NOT NULL REFERENCES company(id),
  primary_role_id BIGINT NOT NULL REFERENCES roles(id),
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  email VARCHAR(200) NOT NULL UNIQUE,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE company
  ADD CONSTRAINT fk_company_owner_user
  FOREIGN KEY (owner_user_id)
  REFERENCES users(id);

CREATE TABLE teams (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT NOT NULL REFERENCES company(id),
  name VARCHAR(120) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (company_id, name)
);

CREATE TABLE projects (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT NOT NULL REFERENCES company(id),
  project_key VARCHAR(16) NOT NULL,
  name VARCHAR(200) NOT NULL,
  description VARCHAR(255),
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (company_id, project_key)
);

CREATE TABLE team_memberships (
  id BIGSERIAL PRIMARY KEY,
  team_id BIGINT NOT NULL REFERENCES teams(id),
  user_id BIGINT NOT NULL REFERENCES users(id),
  role_id BIGINT NOT NULL REFERENCES roles(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (team_id, user_id)
);

CREATE TABLE project_memberships (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id),
  user_id BIGINT NOT NULL REFERENCES users(id),
  role_id BIGINT NOT NULL REFERENCES roles(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (project_id, user_id)
);

INSERT INTO company (code, name, status, owner_user_id, created_at, updated_at)
VALUES ('phoenixtask-demo', 'PhoenixTask® Demo Company', 'ACTIVE', NULL, NOW(), NOW());

INSERT INTO company_settings (company_id, timezone, locale, week_start, created_at, updated_at)
SELECT id, 'Europe/Madrid', 'es-ES', 'monday', NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO roles (company_id, code, name, description, is_internal, is_assignable, created_at, updated_at)
SELECT id, 'OWNER', 'Owner', 'Internal owner role', true, false, NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO roles (company_id, code, name, description, is_internal, is_assignable, created_at, updated_at)
SELECT id, 'TEAM_LEADER', 'Team Leader', 'Leads teams and projects', false, true, NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO roles (company_id, code, name, description, is_internal, is_assignable, created_at, updated_at)
SELECT id, 'DEVELOPER', 'Developer', 'Builds product and platform', false, true, NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO roles (company_id, code, name, description, is_internal, is_assignable, created_at, updated_at)
SELECT id, 'SUPPORT', 'Support', 'Supports customers and operations', false, true, NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO roles (company_id, code, name, description, is_internal, is_assignable, created_at, updated_at)
SELECT id, 'QUALITY_ASSURANCE', 'Quality Assurance', 'Validates quality and releases', false, true, NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO permissions (code, name, description, created_at) VALUES
  ('company.view', 'Company: View', 'View company profile', NOW()),
  ('company.update', 'Company: Update', 'Update company profile', NOW()),
  ('company.settings.view', 'Company Settings: View', 'View company settings', NOW()),
  ('company.settings.update', 'Company Settings: Update', 'Update company settings', NOW()),
  ('users.view', 'Users: View', 'View users', NOW()),
  ('users.create', 'Users: Create', 'Create users', NOW()),
  ('users.update', 'Users: Update', 'Update users', NOW()),
  ('users.deactivate', 'Users: Deactivate', 'Deactivate users', NOW()),
  ('teams.view', 'Teams: View', 'View teams', NOW()),
  ('teams.create', 'Teams: Create', 'Create teams', NOW()),
  ('teams.update', 'Teams: Update', 'Update teams', NOW()),
  ('teams.delete', 'Teams: Delete', 'Delete teams', NOW()),
  ('team.members.view', 'Team Members: View', 'View team members', NOW()),
  ('team.members.add', 'Team Members: Add', 'Add team members', NOW()),
  ('team.members.remove', 'Team Members: Remove', 'Remove team members', NOW()),
  ('team.members.role.update', 'Team Members: Update Role', 'Update team member roles', NOW()),
  ('projects.view', 'Projects: View', 'View projects', NOW()),
  ('projects.create', 'Projects: Create', 'Create projects', NOW()),
  ('projects.update', 'Projects: Update', 'Update projects', NOW()),
  ('projects.archive', 'Projects: Archive', 'Archive projects', NOW()),
  ('projects.delete', 'Projects: Delete', 'Delete projects', NOW()),
  ('project.members.view', 'Project Members: View', 'View project members', NOW()),
  ('project.members.add', 'Project Members: Add', 'Add project members', NOW()),
  ('project.members.remove', 'Project Members: Remove', 'Remove project members', NOW()),
  ('project.members.role.update', 'Project Members: Update Role', 'Update project member roles', NOW()),
  ('roles.view', 'Roles: View', 'View roles', NOW()),
  ('roles.assign', 'Roles: Assign', 'Assign roles', NOW()),
  ('permissions.view', 'Permissions: View', 'View permissions', NOW());

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
CROSS JOIN permissions p
WHERE c.code = 'phoenixtask-demo' AND r.code = 'OWNER';

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'company.view',
  'company.settings.view',
  'users.view',
  'teams.view',
  'teams.create',
  'teams.update',
  'teams.delete',
  'team.members.view',
  'team.members.add',
  'team.members.remove',
  'team.members.role.update',
  'projects.view',
  'projects.create',
  'projects.update',
  'projects.archive',
  'project.members.view',
  'project.members.add',
  'project.members.remove',
  'project.members.role.update',
  'permissions.view'
)
WHERE c.code = 'phoenixtask-demo' AND r.code = 'TEAM_LEADER';

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'company.view',
  'company.settings.view',
  'teams.view',
  'team.members.view',
  'projects.view',
  'project.members.view',
  'permissions.view'
)
WHERE c.code = 'phoenixtask-demo' AND r.code = 'DEVELOPER';

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'company.view',
  'company.settings.view',
  'users.view',
  'teams.view',
  'team.members.view',
  'projects.view',
  'project.members.view',
  'permissions.view'
)
WHERE c.code = 'phoenixtask-demo' AND r.code = 'SUPPORT';

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN company c ON c.id = r.company_id
JOIN permissions p ON p.code IN (
  'company.view',
  'company.settings.view',
  'users.view',
  'teams.view',
  'team.members.view',
  'projects.view',
  'project.members.view',
  'permissions.view'
)
WHERE c.code = 'phoenixtask-demo' AND r.code = 'QUALITY_ASSURANCE';

INSERT INTO users (company_id, primary_role_id, first_name, last_name, email, status, created_at, updated_at)
SELECT c.id, r.id, 'Sofia', 'Ramos', 'sofia.ramos@phoenixtask.demo', 'ACTIVE', NOW(), NOW()
FROM company c
JOIN roles r ON r.company_id = c.id AND r.code = 'OWNER'
WHERE c.code = 'phoenixtask-demo';

INSERT INTO users (company_id, primary_role_id, first_name, last_name, email, status, created_at, updated_at)
SELECT c.id, r.id, 'Mateo', 'Cruz', 'mateo.cruz@phoenixtask.demo', 'ACTIVE', NOW(), NOW()
FROM company c
JOIN roles r ON r.company_id = c.id AND r.code = 'TEAM_LEADER'
WHERE c.code = 'phoenixtask-demo';

INSERT INTO users (company_id, primary_role_id, first_name, last_name, email, status, created_at, updated_at)
SELECT c.id, r.id, 'Lucia', 'Vega', 'lucia.vega@phoenixtask.demo', 'ACTIVE', NOW(), NOW()
FROM company c
JOIN roles r ON r.company_id = c.id AND r.code = 'DEVELOPER'
WHERE c.code = 'phoenixtask-demo';

INSERT INTO users (company_id, primary_role_id, first_name, last_name, email, status, created_at, updated_at)
SELECT c.id, r.id, 'Pablo', 'Ruiz', 'pablo.ruiz@phoenixtask.demo', 'ACTIVE', NOW(), NOW()
FROM company c
JOIN roles r ON r.company_id = c.id AND r.code = 'SUPPORT'
WHERE c.code = 'phoenixtask-demo';

INSERT INTO users (company_id, primary_role_id, first_name, last_name, email, status, created_at, updated_at)
SELECT c.id, r.id, 'Elena', 'Torres', 'elena.torres@phoenixtask.demo', 'ACTIVE', NOW(), NOW()
FROM company c
JOIN roles r ON r.company_id = c.id AND r.code = 'QUALITY_ASSURANCE'
WHERE c.code = 'phoenixtask-demo';

UPDATE company
SET owner_user_id = (
  SELECT id FROM users WHERE email = 'sofia.ramos@phoenixtask.demo'
)
WHERE code = 'phoenixtask-demo';

ALTER TABLE company
  ALTER COLUMN owner_user_id SET NOT NULL;

INSERT INTO teams (company_id, name, description, created_at, updated_at)
SELECT id, 'Product', 'Product strategy and roadmap', NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO teams (company_id, name, description, created_at, updated_at)
SELECT id, 'Engineering', 'Platform and delivery', NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO teams (company_id, name, description, created_at, updated_at)
SELECT id, 'Customer Success', 'Customer success operations', NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO projects (company_id, project_key, name, description, status, created_at, updated_at)
SELECT id, 'PTW', 'PhoenixTask Web', 'Web experience and UI', 'ACTIVE', NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO projects (company_id, project_key, name, description, status, created_at, updated_at)
SELECT id, 'CORE', 'Platform Core', 'Core services and data', 'ACTIVE', NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO projects (company_id, project_key, name, description, status, created_at, updated_at)
SELECT id, 'CSOPS', 'Customer Success Ops', 'Operations workflows', 'ACTIVE', NOW(), NOW()
FROM company
WHERE code = 'phoenixtask-demo';

INSERT INTO team_memberships (team_id, user_id, role_id, created_at)
SELECT t.id, u.id, r.id, NOW()
FROM teams t
JOIN users u ON u.email = 'sofia.ramos@phoenixtask.demo'
JOIN roles r ON r.code = 'TEAM_LEADER' AND r.company_id = t.company_id
WHERE t.name = 'Product';

INSERT INTO team_memberships (team_id, user_id, role_id, created_at)
SELECT t.id, u.id, r.id, NOW()
FROM teams t
JOIN users u ON u.email = 'mateo.cruz@phoenixtask.demo'
JOIN roles r ON r.code = 'TEAM_LEADER' AND r.company_id = t.company_id
WHERE t.name = 'Product';

INSERT INTO team_memberships (team_id, user_id, role_id, created_at)
SELECT t.id, u.id, r.id, NOW()
FROM teams t
JOIN users u ON u.email = 'elena.torres@phoenixtask.demo'
JOIN roles r ON r.code = 'QUALITY_ASSURANCE' AND r.company_id = t.company_id
WHERE t.name = 'Product';

INSERT INTO team_memberships (team_id, user_id, role_id, created_at)
SELECT t.id, u.id, r.id, NOW()
FROM teams t
JOIN users u ON u.email = 'mateo.cruz@phoenixtask.demo'
JOIN roles r ON r.code = 'TEAM_LEADER' AND r.company_id = t.company_id
WHERE t.name = 'Engineering';

INSERT INTO team_memberships (team_id, user_id, role_id, created_at)
SELECT t.id, u.id, r.id, NOW()
FROM teams t
JOIN users u ON u.email = 'lucia.vega@phoenixtask.demo'
JOIN roles r ON r.code = 'DEVELOPER' AND r.company_id = t.company_id
WHERE t.name = 'Engineering';

INSERT INTO team_memberships (team_id, user_id, role_id, created_at)
SELECT t.id, u.id, r.id, NOW()
FROM teams t
JOIN users u ON u.email = 'elena.torres@phoenixtask.demo'
JOIN roles r ON r.code = 'QUALITY_ASSURANCE' AND r.company_id = t.company_id
WHERE t.name = 'Engineering';

INSERT INTO team_memberships (team_id, user_id, role_id, created_at)
SELECT t.id, u.id, r.id, NOW()
FROM teams t
JOIN users u ON u.email = 'pablo.ruiz@phoenixtask.demo'
JOIN roles r ON r.code = 'SUPPORT' AND r.company_id = t.company_id
WHERE t.name = 'Customer Success';

INSERT INTO team_memberships (team_id, user_id, role_id, created_at)
SELECT t.id, u.id, r.id, NOW()
FROM teams t
JOIN users u ON u.email = 'sofia.ramos@phoenixtask.demo'
JOIN roles r ON r.code = 'TEAM_LEADER' AND r.company_id = t.company_id
WHERE t.name = 'Customer Success';

INSERT INTO project_memberships (project_id, user_id, role_id, created_at)
SELECT p.id, u.id, r.id, NOW()
FROM projects p
JOIN users u ON u.email = 'mateo.cruz@phoenixtask.demo'
JOIN roles r ON r.code = 'TEAM_LEADER' AND r.company_id = p.company_id
WHERE p.project_key = 'PTW';

INSERT INTO project_memberships (project_id, user_id, role_id, created_at)
SELECT p.id, u.id, r.id, NOW()
FROM projects p
JOIN users u ON u.email = 'lucia.vega@phoenixtask.demo'
JOIN roles r ON r.code = 'DEVELOPER' AND r.company_id = p.company_id
WHERE p.project_key = 'PTW';

INSERT INTO project_memberships (project_id, user_id, role_id, created_at)
SELECT p.id, u.id, r.id, NOW()
FROM projects p
JOIN users u ON u.email = 'elena.torres@phoenixtask.demo'
JOIN roles r ON r.code = 'QUALITY_ASSURANCE' AND r.company_id = p.company_id
WHERE p.project_key = 'PTW';

INSERT INTO project_memberships (project_id, user_id, role_id, created_at)
SELECT p.id, u.id, r.id, NOW()
FROM projects p
JOIN users u ON u.email = 'mateo.cruz@phoenixtask.demo'
JOIN roles r ON r.code = 'TEAM_LEADER' AND r.company_id = p.company_id
WHERE p.project_key = 'CORE';

INSERT INTO project_memberships (project_id, user_id, role_id, created_at)
SELECT p.id, u.id, r.id, NOW()
FROM projects p
JOIN users u ON u.email = 'lucia.vega@phoenixtask.demo'
JOIN roles r ON r.code = 'DEVELOPER' AND r.company_id = p.company_id
WHERE p.project_key = 'CORE';

INSERT INTO project_memberships (project_id, user_id, role_id, created_at)
SELECT p.id, u.id, r.id, NOW()
FROM projects p
JOIN users u ON u.email = 'elena.torres@phoenixtask.demo'
JOIN roles r ON r.code = 'QUALITY_ASSURANCE' AND r.company_id = p.company_id
WHERE p.project_key = 'CORE';

INSERT INTO project_memberships (project_id, user_id, role_id, created_at)
SELECT p.id, u.id, r.id, NOW()
FROM projects p
JOIN users u ON u.email = 'pablo.ruiz@phoenixtask.demo'
JOIN roles r ON r.code = 'SUPPORT' AND r.company_id = p.company_id
WHERE p.project_key = 'CSOPS';

INSERT INTO project_memberships (project_id, user_id, role_id, created_at)
SELECT p.id, u.id, r.id, NOW()
FROM projects p
JOIN users u ON u.email = 'sofia.ramos@phoenixtask.demo'
JOIN roles r ON r.code = 'TEAM_LEADER' AND r.company_id = p.company_id
WHERE p.project_key = 'CSOPS';
