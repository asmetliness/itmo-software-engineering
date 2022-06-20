CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    was_read boolean default false,
    message TEXT,
    user_id BIGINT REFERENCES users(id),
    order_id BIGINT REFERENCES orders(id)
)