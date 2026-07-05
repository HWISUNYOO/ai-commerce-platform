package com.aicommerce.order.service;

import com.aicommerce.order.domain.Order;
import com.aicommerce.order.domain.OrderItem;
import com.aicommerce.order.event.OrderCreatedEvent;
import com.aicommerce.order.event.OrderEventPublisher;
import com.aicommerce.order.exception.NotFoundException;
import com.aicommerce.order.repository.OrderRepository;
import com.aicommerce.order.web.dto.OrderCreateRequest;
import com.aicommerce.order.web.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderEventPublisher eventPublisher;

	@Transactional
	public OrderResponse create(OrderCreateRequest request) {
		Order order = Order.builder()
				.memberId(request.memberId())
				.build();
		for (OrderCreateRequest.OrderItemRequest item : request.items()) {
			order.addItem(OrderItem.builder()
					.productId(item.productId())
					.productName(item.productName())
					.unitPrice(item.unitPrice())
					.quantity(item.quantity())
					.build());
		}
		Order saved = orderRepository.save(order);
		eventPublisher.publishOrderCreated(
				new OrderCreatedEvent(saved.getId(), saved.getMemberId(), saved.getTotalAmount()));
		return OrderResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public OrderResponse get(Long id) {
		return orderRepository.findById(id)
				.map(OrderResponse::from)
				.orElseThrow(() -> new NotFoundException("Order", id));
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> list() {
		return orderRepository.findAll().stream()
				.map(OrderResponse::from)
				.toList();
	}
}
