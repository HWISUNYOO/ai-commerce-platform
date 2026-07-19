package com.aicommerce.point.event;

import java.math.BigDecimal;

/**
 * payment-service 가 결제 승인 후 발행하는 이벤트("payment.approved").
 * point-service 는 이를 구독해 회원에게 포인트를 적립한다.
 * (각 서비스가 자신이 소비하는 이벤트 계약을 자기 패키지에 소유한다 — MSA 계약 소유 원칙)
 */
public record PaymentApprovedEvent(
		Long paymentId,
		Long orderId,
		Long memberId,
		BigDecimal amount) {
}
