package com.aicommerce.payment.event;

import com.aicommerce.payment.domain.PaymentMethod;
import com.aicommerce.payment.service.PaymentService;
import com.aicommerce.payment.web.dto.PaymentCreateRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 주문 생성 이벤트를 구독해 해당 주문의 결제를 생성한다.
 * (order-service 를 직접 REST 호출하지 않고 Kafka 이벤트로 느슨하게 연결)
 */
@Component
@RequiredArgsConstructor
public class OrderEventListener {

	private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

	private final PaymentService paymentService;

	@KafkaListener(topics = "order.created", groupId = "payment-service")
	public void onOrderCreated(OrderCreatedEvent event) {
		log.info("Received OrderCreated: orderId={}, amount={}", event.orderId(), event.totalAmount());
		paymentService.create(new PaymentCreateRequest(event.orderId(), event.totalAmount(), PaymentMethod.CARD));
		log.info("Payment created for orderId={}", event.orderId());
	}
}
