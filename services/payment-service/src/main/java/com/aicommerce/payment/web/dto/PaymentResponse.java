package com.aicommerce.payment.web.dto;

import com.aicommerce.payment.domain.Payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
		Long id,
		Long orderId,
		BigDecimal amount,
		String method,
		String status,
		Instant approvedAt,
		Instant createdAt) {

	public static PaymentResponse from(Payment payment) {
		return new PaymentResponse(
				payment.getId(),
				payment.getOrderId(),
				payment.getAmount(),
				payment.getMethod().name(),
				payment.getStatus().name(),
				payment.getApprovedAt(),
				payment.getCreatedAt());
	}
}
