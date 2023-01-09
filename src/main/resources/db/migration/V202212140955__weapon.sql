
create table weapon (
    id BIGSERIAL PRIMARY KEY,

    title TEXT NOT NULL,
    description TEXT,
    price DECIMAL(18, 2) NOT NULL,

    creation_date DATE,
    created_user_id BIGINT REFERENCES users(id) NOT NULL,
    requested_user_id BIGINT NULL REFERENCES users(id),
    acquired_user_id BIGINT NULL REFERENCES users(id),
    suggested_courier_id BIGINT NULL REFERENCES users(id),
    accepted_courier_id BIGINT NULL REFERENCES users(id),
    status_id BIGINT REFERENCES status(id) NOT NULL
)