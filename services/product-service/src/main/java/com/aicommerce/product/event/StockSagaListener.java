package com.aicommerce.product.event;

import com.aicommerce.product.exception.InsufficientStockException;
import com.aicommerce.product.exception.NotFoundException;
import com.aicommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 재고 소유 서비스로서 Saga 의 재고 단계를 담당한다.
 *
 * <ul>
 *   <li>order.created → 재고 예약 시도. 성공 stock.reserved / 부족 stock.rejected 발행</li>
 *   <li>payment.failed → 예약 재고 복원(보상)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class StockSagaListener {

	private static final Logger log = LoggerFactory.getLogger(StockSagaListener.class);

	private final ProductService productService;
	private final StockEventPublisher publisher;

	@KafkaListener(topics = "order.created", groupId = "product-service",
			containerFactory = "orderCreatedFactory")
	public void onOrderCreated(OrderCreatedEvent event) {
		try {
			productService.reserveForOrder(event.orderId(), event.items());
			publisher.publishStockReserved(
					new StockReservedEvent(event.orderId(), event.memberId(), event.totalAmount()));
			log.info("Stock reserved for order {}", event.orderId());
		} catch (InsufficientStockException | NotFoundException e) {
			publisher.publishStockRejected(
					new StockRejectedEvent(event.orderId(), event.memberId(), e.getMessage()));
			log.warn("Stock rejected for order {}: {}", event.orderId(), e.getMessage());
		}
	}

	@KafkaListener(topics = "payment.failed", groupId = "product-service",
			containerFactory = "paymentFailedFactory")
	public void onPaymentFailed(PaymentFailedEvent event) {
		productService.releaseForOrder(event.orderId());
		log.info("Compensation: stock released for order {} ({})", event.orderId(), event.reason());
	}
}
