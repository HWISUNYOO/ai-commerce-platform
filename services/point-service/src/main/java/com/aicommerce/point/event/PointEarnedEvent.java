package com.aicommerce.point.event;

/**
 * 포인트 적립이 완료되면 발행하는 이벤트("point.earned").
 * notification-service 가 이를 구독해 적립 알림을 발송한다.
 */
public record PointEarnedEvent(
		Long memberId,
		Long orderId,
		long earnedPoints,
		long balance) {
}
