package com.aicommerce.product.event;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEventPublisher {

	public static final String RESERVED_TOPIC = "stock.reserved";
	public static final String REJECTED_TOPIC = "stock.rejected";

	private static final Logger log = LoggerFactory.getLogger(StockEventPublisher.class);

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Value("${app.events.enabled:true}")
	private boolean enabled;

	public void publishStockReserved(StockReservedEvent event) {
		if (!enabled) {
			return;
		}
		kafkaTemplate.send(RESERVED_TOPIC, String.valueOf(event.orderId()), event)
				.whenComplete((r, ex) -> logResult("StockReserved", event.orderId(), ex));
	}

	public void publishStockRejected(StockRejectedEvent event) {
		if (!enabled) {
			return;
		}
		kafkaTemplate.send(REJECTED_TOPIC, String.valueOf(event.orderId()), event)
				.whenComplete((r, ex) -> logResult("StockRejected", event.orderId(), ex));
	}

	private void logResult(String name, Long orderId, Throwable ex) {
		if (ex != null) {
			log.error("Failed to publish {}: orderId={}", name, orderId, ex);
		} else {
			log.info("Published {}: orderId={}", name, orderId);
		}
	}
}
