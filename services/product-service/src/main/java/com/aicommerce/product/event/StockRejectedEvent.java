package com.aicommerce.product.event;

/**
 * 재고 부족으로 예약 실패 시 발행("stock.rejected").
 * order-service(주문 취소)·notification-service(취소 알림)가 구독한다.
 * (알림을 위해 memberId 도 함께 싣는다 — order-service 는 이 필드를 무시한다.)
 */
public record StockRejectedEvent(
		Long orderId,
		Long memberId,
		String reason) {
}
