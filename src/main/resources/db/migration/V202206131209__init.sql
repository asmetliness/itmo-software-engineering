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

    login Text,
    password_hash Text
);