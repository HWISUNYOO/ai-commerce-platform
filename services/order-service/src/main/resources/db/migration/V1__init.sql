CREATE TABLE orders (
    id           BIGSERIAL     PRIMARY KEY,
    member_id    BIGINT        NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL
);

CREATE TABLE order_items (
    id           BIGSERIAL     PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES orders (id),
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(255)  NOT NULL,
    unit_price   NUMERIC(12,2) NOT NULL,
    quantity     INTEGER       NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
