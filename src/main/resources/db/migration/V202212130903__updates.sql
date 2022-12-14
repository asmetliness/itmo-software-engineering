insert INTO status (id, name)
VALUES
    (8, 'Отменен');

ALTER TABLE orders
    ADD COLUMN accepted_courier_id BIGINT NULL REFERENCES users(id);

ALTER TABLE artifact
    ADD COLUMN average_days INT NOT NULL DEFAULT 1;