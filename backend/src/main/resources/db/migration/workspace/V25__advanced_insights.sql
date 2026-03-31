ALTER TABLE issues
  ADD COLUMN is_tech_debt BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE issues
SET is_tech_debt = TRUE
WHERE issue_key IN ('CORE-1');
