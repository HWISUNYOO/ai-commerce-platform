package com.aicommerce.payment.event;

import java.math.BigDecimal;

/**
 * order-service 가 발행하는 "order.created" 이벤트의 소비용 모델.
 * (MSA 원칙상 각 서비스가 자신의 이벤트 계약을 소유한다 — order 쪽과 동일 스키마.)
 */
public record OrderCreatedEvent(
		Long orderId,
		Long memberId,
		BigDecimal totalAmount) {
}
