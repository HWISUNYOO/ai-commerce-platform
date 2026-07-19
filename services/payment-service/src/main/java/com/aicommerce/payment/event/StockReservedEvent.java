package com.aicommerce.payment.event;

import java.math.BigDecimal;

/**
 * product-service 의 "stock.reserved"(구독용). 재고가 예약되면 이 이벤트를 받아 결제를 진행한다.
 * (Saga 재배선: 결제 트리거가 order.created → stock.reserved 로 바뀌었다.)
 */
public record StockReservedEvent(
		Long orderId,
		Long memberId,
		BigDecimal amount) {
}
