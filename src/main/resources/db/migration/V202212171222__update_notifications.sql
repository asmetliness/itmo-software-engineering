ALTER TABLE notifications
    ADD COLUMN weapon_order_id BIGINT REFERENCES weapon(id),
    ADD COLUMN information_order_id BIGINT REFERENCES information(id)