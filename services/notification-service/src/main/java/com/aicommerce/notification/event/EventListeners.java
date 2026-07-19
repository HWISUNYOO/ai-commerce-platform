package com.aicommerce.notification.event;

import com.aicommerce.notification.domain.NotificationType;
import com.aicommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 주문/결제/포인트 이벤트를 구독해 알림을 발송한다.
 *
 * <p>Kafka at-least-once 로 같은 이벤트가 중복 전달될 수 있으므로,
 * (이벤트종류 + 주문번호) 멱등키로 Redis SETNX 를 걸어 중복 알림을 막는다.
 */
@Component
@RequiredArgsConstructor
public class EventListeners {

	private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(1);

	private final NotificationService notificationService;
	private final StringRedisTemplate redisTemplate;

	private boolean firstDelivery(String key) {
		Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", IDEMPOTENCY_TTL);
		return !Boolean.FALSE.equals(first);
	}

	@KafkaListener(topics = "order.created", groupId = "notification-service",
			containerFactory = "orderCreatedFactory")
	public void onOrderCreated(OrderCreatedEvent event) {
		if (!firstDelivery("notif:idem:order-created:" + event.orderId())) {
			return;
		}
		notificationService.send(event.memberId(), NotificationType.ORDER_CREATED, event.orderId(),
				"주문이 접수되었습니다. (주문번호 " + event.orderId() + ")");
	}

	@KafkaListener(topics = "payment.approved", groupId = "notification-service",
			containerFactory = "paymentApprovedFactory")
	public void onPaymentApproved(PaymentApprovedEvent event) {
		if (!firstDelivery("notif:idem:payment-approved:" + event.orderId())) {
			return;
		}
		notificationService.send(event.memberId(), NotificationType.PAYMENT_APPROVED, event.orderId(),
				"결제가 완료되었습니다. " + event.amount() + "원 (주문번호 " + event.orderId() + ")");
	}

	@KafkaListener(topics = "point.earned", groupId = "notification-service",
			containerFactory = "pointEarnedFactory")
	public void onPointEarned(PointEarnedEvent event) {
		if (!firstDelivery("notif:idem:point-earned:" + event.orderId())) {
			return;
		}
		notificationService.send(event.memberId(), NotificationType.POINT_EARNED, event.orderId(),
				event.earnedPoints() + "포인트가 적립되었습니다. (현재 " + event.balance() + "P)");
	}

	@KafkaListener(topics = "stock.rejected", groupId = "notification-service",
			containerFactory = "stockRejectedFactory")
	public void onStockRejected(StockRejectedEvent event) {
		if (!firstDelivery("notif:idem:stock-rejected:" + event.orderId())) {
			return;
		}
		notificationService.send(event.memberId(), NotificationType.ORDER_CANCELLED, event.orderId(),
				"주문이 취소되었습니다. 재고가 부족합니다. (주문번호 " + event.orderId() + ")");
	}

	@KafkaListener(topics = "payment.failed", groupId = "notification-service",
			containerFactory = "paymentFailedFactory")
	public void onPaymentFailed(PaymentFailedEvent event) {
		if (!firstDelivery("notif:idem:payment-failed:" + event.orderId())) {
			return;
		}
		notificationService.send(event.memberId(), NotificationType.ORDER_CANCELLED, event.orderId(),
				"주문이 취소되었습니다. 결제에 실패했습니다. (주문번호 " + event.orderId() + ")");
	}
}
