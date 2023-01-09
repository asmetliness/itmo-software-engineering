CREATE TABLE status
(
    id BIGSERIAL PRIMARY KEY,
    name TEXT
);


create table orders
(
    id BIGSERIAL PRIMARY KEY,

    artifact_id BIGSERIAL REFERENCES artifact (id) NOT NULL,

    price DECIMAL(18, 2) NOT NULL,

    completion_date DATE,
    created_user_id BIGINT REFERENCES users(id) NOT NULL,
    accepted_user_id BIGINT NULL REFERENCES users(id),
    assigned_user_id BIGINT NULL REFERENCES users(id),
    suggested_user_id BIGINT NULL REFERENCES users(id),
    status_id BIGINT REFERENCES status(id) NOT NULL

);

insert INTO status (id, name)
VALUES
    (1, 'Новый заказ'),
    (2, 'Принят барыгой'),
    (3, 'Принят сталкером'),
    (4, 'Выполняется сталкером'),
    (5, 'Передан барыге'),
    (6, 'Отправлен курьером'),
    (7, 'Выполнен')
