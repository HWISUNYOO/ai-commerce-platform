package com.aicommerce.order.event;

/** product-service 의 "stock.rejected"(구독용). 재고 부족 → 주문 CANCELLED. */
public record StockRejectedEvent(
		Long orderId,
		String reason) {
}
