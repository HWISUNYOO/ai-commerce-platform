package com.aicommerce.order.event;

import java.math.BigDecimal;

/** payment-service 의 "payment.approved"(구독용). 수신 시 주문을 CONFIRMED 로 전이. */
public record PaymentApprovedEvent(
		Long paymentId,
		Long orderId,
		Long memberId,
		BigDecimal amount) {
}
