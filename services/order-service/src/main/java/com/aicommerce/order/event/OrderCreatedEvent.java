package com.aicommerce.order.event;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문이 생성되면 Kafka("order.created")로 발행되는 이벤트.
 * product-service(재고 예약)·notification-service 등이 구독한다.
 *
 * <p>Saga 전환으로 재고 예약이 이벤트 기반이 되면서, product-service 가 어떤 상품을
 * 얼마나 예약할지 알 수 있도록 items 를 함께 싣는다.
 */
public record OrderCreatedEvent(
		Long orderId,
		Long memberId,
		BigDecimal totalAmount,
		List<Item> items) {

	public record Item(Long productId, int quantity) {
	}
}
