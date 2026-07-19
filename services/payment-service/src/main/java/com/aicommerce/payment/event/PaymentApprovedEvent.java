package com.aicommerce.payment.event;

import java.math.BigDecimal;

/**
 * 결제 승인 후 발행하는 이벤트("payment.approved").
 * point-service(포인트 적립) / notification-service(알림) 등이 구독한다.
 *
 * <p>memberId 는 Payment 엔티티가 보유하지 않으므로, 원본 order.created 이벤트에서
 * 전달받은 값을 그대로 실어 보낸다(OrderEventListener 에서 발행).
 */
public record PaymentApprovedEvent(
		Long paymentId,
		Long orderId,
		Long memberId,
		BigDecimal amount) {
}
