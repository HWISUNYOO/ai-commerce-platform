package com.aicommerce.point.event;

import com.aicommerce.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 결제 승인 이벤트를 구독해 포인트를 적립하고, 적립 완료 이벤트를 발행한다.
 *
 * <p>멱등성 이중 방어: Kafka at-least-once 로 같은 이벤트가 두 번 올 수 있다.
 * ① Redis SETNX 로 빠르게 중복을 거르고, ② point_transactions.order_id UNIQUE 제약이
 * 최종 방어선이 된다(적립은 금전이므로 DB 레벨 보장을 둔다).
 */
@Component
@RequiredArgsConstructor
public class PaymentApprovedListener {

	private static final Logger log = LoggerFactory.getLogger(PaymentApprovedListener.class);
	private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(1);

	private final PointService pointService;
	private final PointEventPublisher publisher;
	private final StringRedisTemplate redisTemplate;

	@Value("${app.point.earn-rate:0.01}")
	private BigDecimal earnRate;

	@KafkaListener(topics = "payment.approved", groupId = "point-service")
	public void onPaymentApproved(PaymentApprovedEvent event) {
		String idempotencyKey = "point:idem:order:" + event.orderId();
		Boolean firstDelivery = redisTemplate.opsForValue()
				.setIfAbsent(idempotencyKey, "1", IDEMPOTENCY_TTL);

		if (Boolean.FALSE.equals(firstDelivery)) {
			log.warn("Duplicate payment.approved ignored (idempotent): orderId={}", event.orderId());
			return;
		}

		log.info("Received PaymentApproved: orderId={}, memberId={}, amount={}",
				event.orderId(), event.memberId(), event.amount());

		pointService.earn(event.memberId(), event.orderId(), event.amount(), earnRate)
				.ifPresent(tx -> {
					long balance = pointService.balance(event.memberId());
					publisher.publishPointEarned(
							new PointEarnedEvent(event.memberId(), event.orderId(), tx.getAmount(), balance));
					log.info("Points earned: memberId={}, orderId={}, points={}, balance={}",
							event.memberId(), event.orderId(), tx.getAmount(), balance);
				});
	}
}
