CREATE TABLE point_transactions (
    id         BIGSERIAL   PRIMARY KEY,
    member_id  BIGINT      NOT NULL,
    order_id   BIGINT      NOT NULL UNIQUE,   -- 주문당 1회 적립(멱등성 DB 보장)
    amount     BIGINT      NOT NULL,          -- 적립 포인트(EARN 양수)
    type       VARCHAR(20) NOT NULL,
    created_at TIMESTAMP   NOT NULL
);

CREATE INDEX idx_point_tx_member ON point_transactions (member_id);
