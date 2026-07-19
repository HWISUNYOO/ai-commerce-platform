package com.aicommerce.notification.event;

import java.math.BigDecimal;

/** payment-service 가 발행하는 "payment.approved" 이벤트(구독용 계약). */
public record PaymentApprovedEvent(
		Long paymentId,
		Long orderId,
		Long memberId,
		BigDecimal amount) {
}
