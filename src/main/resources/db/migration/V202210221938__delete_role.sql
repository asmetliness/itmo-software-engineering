ALTER TABLE users
ADD COLUMN role TEXT,
ADD COLUMN nickname TEXT;

-- ХЗ правильно ли это будет работать на реальных данных
UPDATE users SET role = (
    SELECT roles.name
    FROM users, roles
    WHERE users.role_id = roles.id
);

ALTER TABLE users
DROP COLUMN role_id;

DROP TABLE roles