CREATE TABLE notifications (
    id           BIGSERIAL    PRIMARY KEY,
    member_id    BIGINT       NOT NULL,
    type         VARCHAR(30)  NOT NULL,   -- ORDER_CREATED / PAYMENT_APPROVED / POINT_EARNED
    reference_id BIGINT,                  -- 관련 주문번호(추적용)
    message      VARCHAR(500) NOT NULL,
    channel      VARCHAR(20)  NOT NULL,   -- 발송 채널(현재 mock: LOG)
    status       VARCHAR(20)  NOT NULL,   -- SENT
    created_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_notif_member ON notifications (member_id);
