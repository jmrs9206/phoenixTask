ALTER TABLE users ADD COLUMN password_hash VARCHAR(255);

UPDATE users
SET password_hash = '$2a$12$nJk1D3Cct1eHobNT0A89PON6G1Cleq52vcC2az4kgouthP/EppSyi'
WHERE password_hash IS NULL;

ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;
