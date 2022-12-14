
create table weapon (
    id BIGSERIAL PRIMARY KEY,

    title TEXT,
    description TEXT,
    price DECIMAL(18, 2),

    creation_date DATE,
    created_user_id BIGINT REFERENCES users(id),
    requested_user_id BIGINT NULL REFERENCES users(id),
    acquired_user_id BIGINT NULL REFERENCES users(id),
    suggested_courier_id BIGINT NULL REFERENCES users(id),
    accepted_courier_id BIGINT NULL REFERENCES users(id),
    status_id BIGINT REFERENCES status(id)
)