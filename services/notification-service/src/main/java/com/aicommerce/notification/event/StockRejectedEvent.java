package com.aicommerce.notification.event;

/** product-service 의 "stock.rejected"(구독용). 재고 부족 → 주문 취소 알림. */
public record StockRejectedEvent(
		Long orderId,
		Long memberId,
		String reason) {
}
