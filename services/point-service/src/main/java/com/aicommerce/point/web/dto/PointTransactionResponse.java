package com.aicommerce.point.web.dto;

import com.aicommerce.point.domain.PointTransaction;

import java.time.Instant;

public record PointTransactionResponse(
		Long id,
		Long orderId,
		long amount,
		String type,
		Instant createdAt) {

	public static PointTransactionResponse from(PointTransaction tx) {
		return new PointTransactionResponse(
				tx.getId(),
				tx.getOrderId(),
				tx.getAmount(),
				tx.getType().name(),
				tx.getCreatedAt());
	}
}
