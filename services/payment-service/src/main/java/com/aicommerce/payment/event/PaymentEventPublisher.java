package com.aicommerce.payment.event;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

	public static final String TOPIC = "payment.approved";

	private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

	private final KafkaTemplate<String, PaymentApprovedEvent> kafkaTemplate;

	@Value("${app.events.enabled:true}")
	private boolean enabled;

	public void publishPaymentApproved(PaymentApprovedEvent event) {
		if (!enabled) {
			return;
		}
		kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event)
				.whenComplete((result, ex) -> {
					if (ex != null) {
						log.error("Failed to publish PaymentApproved: orderId={}", event.orderId(), ex);
					} else {
						log.info("Published PaymentApproved: orderId={}", event.orderId());
					}
				});
	}
}
