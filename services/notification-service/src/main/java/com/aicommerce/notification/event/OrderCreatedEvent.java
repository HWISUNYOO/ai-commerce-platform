package com.aicommerce.notification.event;

import java.math.BigDecimal;

/** order-service 가 발행하는 "order.created" 이벤트(구독용 계약). */
public record OrderCreatedEvent(
		Long orderId,
		Long memberId,
		BigDecimal totalAmount) {
}
