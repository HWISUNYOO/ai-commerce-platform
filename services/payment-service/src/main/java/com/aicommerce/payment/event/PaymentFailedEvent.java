package com.aicommerce.payment.event;

import java.math.BigDecimal;

/**
 * 결제 실패 시 발행("payment.failed").
 * order-service(주문 취소)·product-service(재고 복원 보상)가 구독한다.
 */
public record PaymentFailedEvent(
		Long orderId,
		Long memberId,
		BigDecimal amount,
		String reason) {
}
