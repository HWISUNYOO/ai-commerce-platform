package com.aicommerce.notification.web.dto;

import com.aicommerce.notification.domain.Notification;

import java.time.Instant;

public record NotificationResponse(
		Long id,
		Long memberId,
		String type,
		Long referenceId,
		String message,
		String channel,
		String status,
		Instant createdAt) {

	public static NotificationResponse from(Notification n) {
		return new NotificationResponse(
				n.getId(),
				n.getMemberId(),
				n.getType().name(),
				n.getReferenceId(),
				n.getMessage(),
				n.getChannel(),
				n.getStatus(),
				n.getCreatedAt());
	}
}
