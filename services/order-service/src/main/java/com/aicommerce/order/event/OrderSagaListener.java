package com.aicommerce.order.event;

import com.aicommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 주문 Saga 의 오케스트레이션을 담당하는 리스너(choreography).
 * 다운스트림 서비스가 발행한 결과 이벤트를 구독해 주문 상태를 전이시킨다.
 *
 * <ul>
 *   <li>payment.approved → 주문 CONFIRMED (성공)</li>
 *   <li>stock.rejected  → 주문 CANCELLED (재고 부족)</li>
 *   <li>payment.failed  → 주문 CANCELLED (결제 실패 보상)</li>
 * </ul>
 *
 * 상태 전이는 Order 내부에서 PENDING 일 때만 일어나므로 중복 이벤트에도 안전하다.
 */
@Component
@RequiredArgsConstructor
public class OrderSagaListener {

	private static final Logger log = LoggerFactory.getLogger(OrderSagaListener.class);

	private final OrderService orderService;

	@KafkaListener(topics = "payment.approved", groupId = "order-service",
			containerFactory = "paymentApprovedFactory")
	public void onPaymentApproved(PaymentApprovedEvent event) {
		log.info("Saga: payment.approved → confirm order {}", event.orderId());
		orderService.confirm(event.orderId());
	}

	@KafkaListener(topics = "stock.rejected", groupId = "order-service",
			containerFactory = "stockRejectedFactory")
	public void onStockRejected(StockRejectedEvent event) {
		log.warn("Saga: stock.rejected → cancel order {} ({})", event.orderId(), event.reason());
		orderService.cancel(event.orderId());
	}

	@KafkaListener(topics = "payment.failed", groupId = "order-service",
			containerFactory = "paymentFailedFactory")
	public void onPaymentFailed(PaymentFailedEvent event) {
		log.warn("Saga: payment.failed → cancel order {} ({})", event.orderId(), event.reason());
		orderService.cancel(event.orderId());
	}
}
