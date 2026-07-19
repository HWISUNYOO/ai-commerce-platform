package com.aicommerce.product.config;

import com.aicommerce.product.event.OrderCreatedEvent;
import com.aicommerce.product.event.PaymentFailedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * product-service 는 order.created(재고 예약)와 payment.failed(보상)를 서로 다른 타입으로 구독한다.
 * 토픽(이벤트)별로 타입을 고정한 컨테이너 팩토리를 만든다.
 */
@Configuration
public class KafkaConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers:localhost:9092}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id:product-service}")
	private String groupId;

	@Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
	private String autoOffsetReset;

	@Value("${spring.kafka.listener.auto-startup:true}")
	private boolean autoStartup;

	private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> type) {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

		JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(type);
		valueDeserializer.setUseTypeHeaders(false);
		valueDeserializer.addTrustedPackages("com.aicommerce.product.event");

		ConcurrentKafkaListenerContainerFactory<String, T> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
				props, new StringDeserializer(), valueDeserializer));
		factory.setAutoStartup(autoStartup);
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedFactory() {
		return factory(OrderCreatedEvent.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedFactory() {
		return factory(PaymentFailedEvent.class);
	}
}
