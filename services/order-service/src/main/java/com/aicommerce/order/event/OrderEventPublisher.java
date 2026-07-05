package com.aicommerce.order.event;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

	public static final String TOPIC = "order.created";

	private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

	private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

	@Value("${app.events.enabled:true}")
	private boolean enabled;

	public void publishOrderCreated(OrderCreatedEvent event) {
		if (!enabled) {
			return;
		}
		kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event)
				.whenComplete((result, ex) -> {
					if (ex != null) {
						log.error("Failed to publish OrderCreated: orderId={}", event.orderId(), ex);
					} else {
						log.info("Published OrderCreated: orderId={}", event.orderId());
					}
				});
	}
}
