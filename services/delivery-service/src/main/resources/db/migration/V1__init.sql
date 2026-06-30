CREATE TABLE deliveries (
    id              BIGSERIAL    PRIMARY KEY,
    order_id        BIGINT       NOT NULL,
    recipient_name  VARCHAR(100) NOT NULL,
    address         VARCHAR(500) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    tracking_number VARCHAR(100),
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);
