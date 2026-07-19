package com.aicommerce.payment.event;

import com.aicommerce.payment.domain.PaymentMethod;
import com.aicommerce.payment.service.PaymentService;
import com.aicommerce.payment.web.dto.PaymentCreateRequest;
import com.aicommerce.payment.web.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 주문 생성 이벤트를 구독해 해당 주문의 결제를 생성한다.
 * (order-service 를 직접 REST 호출하지 않고 Kafka 이벤트로 느슨하게 연결)
 *
 * <p>Kafka 는 at-least-once 전달이라 같은 이벤트가 두 번 올 수 있다.
 * Redis SETNX(멱등키)로 주문당 결제가 한 번만 생성되도록 보장한다.
 */
@Component
@RequiredArgsConstructor
public class OrderEventListener {

	private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
	private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(1);

	private final PaymentService paymentService;
	private final PaymentEventPublisher paymentEventPublisher;
	private final StringRedisTemplate redisTemplate;

	@KafkaListener(topics = "order.created", groupId = "payment-service")
	public void onOrderCreated(OrderCreatedEvent event) {
		String idempotencyKey = "payment:idem:order:" + event.orderId();
		Boolean firstDelivery = redisTemplate.opsForValue()
				.setIfAbsent(idempotencyKey, "1", IDEMPOTENCY_TTL);

		if (Boolean.FALSE.equals(firstDelivery)) {
			log.warn("Duplicate order.created ignored (idempotent): orderId={}", event.orderId());
			return;
		}

		log.info("Received OrderCreated: orderId={}, amount={}", event.orderId(), event.totalAmount());
		PaymentResponse payment = paymentService.create(
				new PaymentCreateRequest(event.orderId(), event.totalAmount(), PaymentMethod.CARD));
		log.info("Payment created for orderId={}", event.orderId());

		// 결제 승인 이벤트 발행 → point-service(적립)·notification-service(알림)가 구독.
		// memberId 는 Payment 가 보유하지 않으므로 원본 이벤트 값을 그대로 전달한다.
		paymentEventPublisher.publishPaymentApproved(new PaymentApprovedEvent(
				payment.id(), event.orderId(), event.memberId(), payment.amount()));
	}
}
