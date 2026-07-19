package com.aicommerce.product.event;

import java.math.BigDecimal;

/** payment-service 의 "payment.failed"(구독용). 수신 시 해당 주문의 재고를 복원(보상)한다. */
public record PaymentFailedEvent(
		Long orderId,
		Long memberId,
		BigDecimal amount,
		String reason) {
}
