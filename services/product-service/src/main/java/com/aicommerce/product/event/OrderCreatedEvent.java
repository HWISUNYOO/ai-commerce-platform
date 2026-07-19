package com.aicommerce.product.event;

import java.math.BigDecimal;
import java.util.List;

/** order-service 의 "order.created"(구독용). 이 이벤트를 받아 재고를 예약한다. */
public record OrderCreatedEvent(
		Long orderId,
		Long memberId,
		BigDecimal totalAmount,
		List<Item> items) {

	public record Item(Long productId, int quantity) {
	}
}
