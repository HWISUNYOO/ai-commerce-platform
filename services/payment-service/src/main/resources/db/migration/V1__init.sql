CREATE TABLE payments (
    id          BIGSERIAL     PRIMARY KEY,
    order_id    BIGINT        NOT NULL,
    amount      NUMERIC(12,2) NOT NULL,
    method      VARCHAR(20)   NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    approved_at TIMESTAMP,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);
