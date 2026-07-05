package com.aicommerce.order.event;

import java.math.BigDecimal;

/**
 * 주문이 생성되면 Kafka("order.created") 로 발행되는 이벤트.
 * 결제·배송 등 다른 서비스가 이 이벤트를 구독해 후속 처리를 한다.
 */
public record OrderCreatedEvent(
		Long orderId,
		Long memberId,
		BigDecimal totalAmount) {
}
