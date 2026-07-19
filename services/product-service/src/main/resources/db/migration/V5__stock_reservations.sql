-- 주문별 재고 예약 기록. 결제 실패/취소 시 이 기록으로 재고를 복원(Saga 보상)한다.
CREATE TABLE stock_reservations (
    id         BIGSERIAL   PRIMARY KEY,
    order_id   BIGINT      NOT NULL,
    product_id BIGINT      NOT NULL,
    quantity   INT         NOT NULL,
    status     VARCHAR(20) NOT NULL,   -- RESERVED / RELEASED
    created_at TIMESTAMP   NOT NULL
);

CREATE INDEX idx_stock_resv_order ON stock_reservations (order_id);
