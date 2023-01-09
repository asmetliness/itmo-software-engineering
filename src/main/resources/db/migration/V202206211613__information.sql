CREATE TABLE information (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    information TEXT NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    creation_date DATE NOT NULL,
    created_user_id BIGINT REFERENCES users(id) NOT NULL,
    accepted_user_id BIGINT REFERENCES users(id),
    status_id BIGINT NOT NULL
)