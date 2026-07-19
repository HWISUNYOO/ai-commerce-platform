package com.aicommerce.payment.event;

import com.aicommerce.payment.domain.PaymentMethod;
import com.aicommerce.payment.service.PaymentService;
import com.aicommerce.payment.web.dto.PaymentCreateRequest;
import com.aicommerce.payment.web.dto.PaymentResponse;
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
 * 재고 예약 완료 이벤트를 구독해 결제를 진행한다(Saga 결제 단계).
 * 승인되면 payment.approved, 거절되면 payment.failed 를 발행한다.
 *
 * <p>Kafka at-least-once 대비 Redis SETNX(멱등키)로 주문당 결제가 한 번만 처리되게 한다.
 * 거절 판정은 데모를 위해 "결제 한도 초과"(app.payment.decline-above) 규칙을 사용한다.
 */
@Component
@RequiredArgsConstructor
public class StockReservedListener {

	private static final Logger log = LoggerFactory.getLogger(StockReservedListener.class);
	private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(1);

	private final PaymentService paymentService;
	private final PaymentEventPublisher publisher;
	private final StringRedisTemplate redisTemplate;

	// 데모용 결제 거절 임계액. 기본값은 매우 커서 일반 주문은 항상 승인된다(환경변수로 낮춰 실패 시연).
	@Value("${app.payment.decline-above:100000000}")
	private BigDecimal declineAbove;

	@KafkaListener(topics = "stock.reserved", groupId = "payment-service")
	public void onStockReserved(StockReservedEvent event) {
		String idempotencyKey = "payment:idem:order:" + event.orderId();
		Boolean firstDelivery = redisTemplate.opsForValue()
				.setIfAbsent(idempotencyKey, "1", IDEMPOTENCY_TTL);

		if (Boolean.FALSE.equals(firstDelivery)) {
			log.warn("Duplicate stock.reserved ignored (idempotent): orderId={}", event.orderId());
			return;
		}

		if (event.amount().compareTo(declineAbove) > 0) {
			publisher.publishPaymentFailed(new PaymentFailedEvent(
					event.orderId(), event.memberId(), event.amount(), "결제 한도 초과"));
			log.warn("Payment declined (over limit): orderId={}, amount={}", event.orderId(), event.amount());
			return;
		}

		PaymentResponse payment = paymentService.create(
				new PaymentCreateRequest(event.orderId(), event.amount(), PaymentMethod.CARD));
		publisher.publishPaymentApproved(new PaymentApprovedEvent(
				payment.id(), event.orderId(), event.memberId(), event.amount()));
		log.info("Payment approved: orderId={}, paymentId={}", event.orderId(), payment.id());
	}
}
