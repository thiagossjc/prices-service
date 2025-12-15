CREATE TABLE IF NOT EXISTS prices (
    id IDENTITY PRIMARY KEY,
    brand_id INT NOT NULL,
    product_id INT NOT NULL,
    price_list INT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    priority INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    curr VARCHAR(10) NOT NULL
);

INSERT INTO prices (brand_id, product_id, price_list, start_date, end_date, priority, price, curr)
VALUES
(1, 35455, 1, TIMESTAMP '2020-06-14 00:00:00', TIMESTAMP '2020-12-31 23:59:59', 0, 35.50, 'EUR'),
(1, 35455, 2, TIMESTAMP '2020-06-14 15:00:00', TIMESTAMP '2020-06-14 18:30:00', 1, 25.45, 'EUR'),
(1, 35455, 3, TIMESTAMP '2020-06-15 00:00:00', TIMESTAMP '2020-06-15 11:00:00', 1, 30.50, 'EUR'),
(1, 35455, 4, TIMESTAMP '2020-06-15 16:00:00', TIMESTAMP '2020-12-31 23:59:59', 1, 38.95, 'EUR');