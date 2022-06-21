CREATE TABLE information (
    id BIGSERIAL PRIMARY KEY,
    title TEXT ,
    description TEXT,
    information TEXT,
    price DECIMAL(18,2),
    creation_date DATE,
    created_user_id BIGINT REFERENCES users(id),
    accepted_user_id BIGINT REFERENCES users(id),
    status_id BIGINT
)