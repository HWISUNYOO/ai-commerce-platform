package com.aicommerce.order.config;

import com.aicommerce.order.event.PaymentApprovedEvent;
import com.aicommerce.order.event.PaymentFailedEvent;
import com.aicommerce.order.event.StockRejectedEvent;
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
 * order-service 는 Saga 결과로 서로 다른 3개 토픽(payment.approved / stock.rejected /
 * payment.failed)을 각기 다른 타입으로 구독한다. 토픽(이벤트)별로 타입을 고정한
 * 컨테이너 팩토리를 만든다.
 */
@Configuration
public class KafkaConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers:localhost:9092}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id:order-service}")
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
		valueDeserializer.addTrustedPackages("com.aicommerce.order.event");

		ConcurrentKafkaListenerContainerFactory<String, T> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
				props, new StringDeserializer(), valueDeserializer));
		factory.setAutoStartup(autoStartup);
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PaymentApprovedEvent> paymentApprovedFactory() {
		return factory(PaymentApprovedEvent.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, StockRejectedEvent> stockRejectedFactory() {
		return factory(StockRejectedEvent.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedFactory() {
		return factory(PaymentFailedEvent.class);
	}
}
