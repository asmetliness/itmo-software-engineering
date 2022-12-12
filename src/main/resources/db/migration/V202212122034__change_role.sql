ALTER TABLE users
ALTER COLUMN role SET DATA TYPE SMALLINT
    USING (
        CASE role WHEN 'Сталкер'         THEN 0
                  WHEN 'Клиент'          THEN 1
                  WHEN 'Барыга'          THEN 2
                  WHEN 'Информатор'      THEN 3
                  WHEN 'Продавец оружия' THEN 4
    )