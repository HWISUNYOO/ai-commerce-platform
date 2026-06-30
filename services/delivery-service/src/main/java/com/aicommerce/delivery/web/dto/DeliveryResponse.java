package com.aicommerce.delivery.web.dto;

import com.aicommerce.delivery.domain.Delivery;

import java.time.Instant;

public record DeliveryResponse(
		Long id,
		Long orderId,
		String recipientName,
		String address,
		String status,
		String trackingNumber,
		Instant createdAt) {

	public static DeliveryResponse from(Delivery delivery) {
		return new DeliveryResponse(
				delivery.getId(),
				delivery.getOrderId(),
				delivery.getRecipientName(),
				delivery.getAddress(),
				delivery.getStatus().name(),
				delivery.getTrackingNumber(),
				delivery.getCreatedAt());
	}
}
