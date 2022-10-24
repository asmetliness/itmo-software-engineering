ALTER TABLE users
ADD COLUMN role TEXT,
ADD COLUMN nickname TEXT;

UPDATE users SET role = roles.name FROM roles WHERE roles.id = users.role_id;

ALTER TABLE users
DROP COLUMN role_id;

DROP TABLE roles