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

	/**
	 * 주문을 PENDING 으로 생성하고 order.created 이벤트를 발행한다(Saga 시작).
	 * 재고 예약은 더 이상 동기 REST 가 아니라 product-service 가 이벤트를 구독해 처리하며,
	 * 결과(stock.reserved→결제 승인 / stock.rejected·payment.failed)에 따라
	 * OrderSagaListener 가 주문을 CONFIRMED/CANCELLED 로 전이시킨다.
	 */
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

		List<OrderCreatedEvent.Item> eventItems = saved.getItems().stream()
				.map(i -> new OrderCreatedEvent.Item(i.getProductId(), i.getQuantity()))
				.toList();
		eventPublisher.publishOrderCreated(new OrderCreatedEvent(
				saved.getId(), saved.getMemberId(), saved.getTotalAmount(), eventItems));
		return OrderResponse.from(saved);
	}

	/** 결제 승인 이벤트 수신 → 주문 확정. */
	@Transactional
	public void confirm(Long orderId) {
		orderRepository.findById(orderId).ifPresent(Order::confirm);
	}

	/** 재고부족/결제실패 이벤트 수신 → 주문 취소(보상). */
	@Transactional
	public void cancel(Long orderId) {
		orderRepository.findById(orderId).ifPresent(Order::cancel);
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
