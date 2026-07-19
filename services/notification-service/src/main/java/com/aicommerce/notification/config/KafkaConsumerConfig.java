package com.aicommerce.notification.config;

import com.aicommerce.notification.event.OrderCreatedEvent;
import com.aicommerce.notification.event.PaymentApprovedEvent;
import com.aicommerce.notification.event.PointEarnedEvent;
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
 * notification-service 는 서로 다른 3개 토픽(order.created / payment.approved / point.earned)을
 * 각기 다른 이벤트 타입으로 구독한다. 단일 default 타입으로는 불가능하므로,
 * 토픽(이벤트)별로 JsonDeserializer 를 고정한 컨테이너 팩토리를 따로 만든다.
 *
 * <p>use.type.headers=false 이므로 프로듀서의 타입 헤더와 무관하게 여기서 지정한 타입으로 역직렬화된다.
 */
@Configuration
public class KafkaConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers:localhost:9092}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id:notification-service}")
	private String groupId;

	@Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
	private String autoOffsetReset;

	// 테스트에서는 spring.kafka.listener.auto-startup=false 로 리스너를 띄우지 않는다.
	@Value("${spring.kafka.listener.auto-startup:true}")
	private boolean autoStartup;

	private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> type) {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

		JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(type);
		valueDeserializer.setUseTypeHeaders(false);
		valueDeserializer.addTrustedPackages("com.aicommerce.notification.event");

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
	public ConcurrentKafkaListenerContainerFactory<String, PaymentApprovedEvent> paymentApprovedFactory() {
		return factory(PaymentApprovedEvent.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PointEarnedEvent> pointEarnedFactory() {
		return factory(PointEarnedEvent.class);
	}
}
