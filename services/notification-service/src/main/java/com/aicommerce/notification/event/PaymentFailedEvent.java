package com.aicommerce.notification.event;

import java.math.BigDecimal;

/** payment-service 의 "payment.failed"(구독용). 결제 실패 → 주문 취소 알림. */
public record PaymentFailedEvent(
		Long orderId,
		Long memberId,
		BigDecimal amount,
		String reason) {
}
