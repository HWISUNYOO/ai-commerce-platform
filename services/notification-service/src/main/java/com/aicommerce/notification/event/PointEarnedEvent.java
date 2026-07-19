package com.aicommerce.notification.event;

/** point-service 가 발행하는 "point.earned" 이벤트(구독용 계약). */
public record PointEarnedEvent(
		Long memberId,
		Long orderId,
		long earnedPoints,
		long balance) {
}
