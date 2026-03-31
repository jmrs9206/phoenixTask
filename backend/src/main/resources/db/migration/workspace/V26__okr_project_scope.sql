ALTER TABLE okr_objectives
  ADD COLUMN project_id BIGINT REFERENCES projects(id);

UPDATE okr_objectives o
SET project_id = kr.project_id
FROM (
  SELECT DISTINCT ON (objective_id) objective_id, project_id
  FROM okr_key_results
  WHERE project_id IS NOT NULL
  ORDER BY objective_id
) kr
WHERE kr.objective_id = o.id
  AND o.project_id IS NULL;

UPDATE okr_objectives o
SET project_id = i.project_id
FROM (
  SELECT DISTINCT ON (objective_id) objective_id, project_id
  FROM okr_initiatives
  WHERE project_id IS NOT NULL
  ORDER BY objective_id
) i
WHERE i.objective_id = o.id
  AND o.project_id IS NULL;

UPDATE okr_objectives o
SET project_id = (
  SELECT p.id
  FROM projects p
  WHERE p.company_id = o.company_id
  ORDER BY p.id
  LIMIT 1
)
WHERE o.project_id IS NULL;

UPDATE okr_key_results kr
SET project_id = o.project_id
FROM okr_objectives o
WHERE kr.objective_id = o.id
  AND kr.project_id IS NULL;

UPDATE okr_initiatives i
SET project_id = iss.project_id
FROM issues iss
WHERE i.issue_id = iss.id
  AND i.project_id IS NULL;

ALTER TABLE okr_objectives
  ALTER COLUMN project_id SET NOT NULL;

ALTER TABLE okr_objectives
  DROP CONSTRAINT IF EXISTS chk_okr_objective_scope;

ALTER TABLE okr_objectives
  DROP COLUMN scope_type,
  DROP COLUMN team_id;

CREATE INDEX IF NOT EXISTS idx_okr_objectives_project_id ON okr_objectives(project_id);
