CREATE TABLE products (
    id             BIGSERIAL     PRIMARY KEY,
    name           VARCHAR(255)  NOT NULL,
    description    VARCHAR(1000),
    price          NUMERIC(12,2) NOT NULL,
    stock_quantity INTEGER       NOT NULL,
    status         VARCHAR(20)   NOT NULL,
    created_at     TIMESTAMP     NOT NULL,
    updated_at     TIMESTAMP     NOT NULL
);
