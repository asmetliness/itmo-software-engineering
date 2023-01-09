CREATE TABLE roles
(
    id BIGSERIAL PRIMARY KEY,
    name TEXT
);


CREATE TABLE users
(
    id BIGSERIAL PRIMARY KEY,
    first_name TEXT,
    last_name TEXT,
    middle_name TEXT,
    role_id BIGSERIAL REFERENCES roles (id),

    email Text NOT NULL,
    password_hash Text NOT NULL
);


INSERT INTO ROLES (name)
VALUES
('stalker'), ('client'), ('huckster'), ('informer');