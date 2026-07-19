package com.aicommerce.product.event;

import java.math.BigDecimal;

/**
 * 재고 예약 성공 시 발행("stock.reserved"). payment-service 가 구독해 결제를 진행한다.
 * 결제에 필요한 memberId·amount 를 원본 order.created 에서 이어받아 함께 싣는다.
 */
public record StockReservedEvent(
		Long orderId,
		Long memberId,
		BigDecimal amount) {
}
