create table artifact (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    price DECIMAL(18,2) NOT NULL
);

insert into artifact (name, price)
VALUES
    ('клык кого-то', 54455.32),
    ('атрефакт 2', 44455.32),
    ('атрефакт 3', 74455.32),
    ('атрефакт 4', 74455.32)
