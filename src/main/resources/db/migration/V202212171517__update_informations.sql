ALTER TABLE notifications
    ADD COLUMN requested_user_id BIGINT REFERENCES users(id)
