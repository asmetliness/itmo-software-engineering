CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    was_read boolean default false,
    message TEXT NOT NULL,
    user_id BIGINT REFERENCES users(id) NOT NULL,
    order_id BIGINT REFERENCES orders(id)
)