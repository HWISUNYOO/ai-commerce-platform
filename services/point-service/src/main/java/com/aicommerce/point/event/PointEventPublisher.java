package com.aicommerce.point.event;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointEventPublisher {

	public static final String TOPIC = "point.earned";

	private static final Logger log = LoggerFactory.getLogger(PointEventPublisher.class);

	private final KafkaTemplate<String, PointEarnedEvent> kafkaTemplate;

	@Value("${app.events.enabled:true}")
	private boolean enabled;

	public void publishPointEarned(PointEarnedEvent event) {
		if (!enabled) {
			return;
		}
		kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event)
				.whenComplete((result, ex) -> {
					if (ex != null) {
						log.error("Failed to publish PointEarned: orderId={}", event.orderId(), ex);
					} else {
						log.info("Published PointEarned: orderId={}, points={}", event.orderId(), event.earnedPoints());
					}
				});
	}
}
