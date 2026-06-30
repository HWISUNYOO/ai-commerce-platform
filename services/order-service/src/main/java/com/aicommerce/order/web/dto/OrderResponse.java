package com.aicommerce.order.web.dto;

import com.aicommerce.order.domain.Order;
import com.aicommerce.order.domain.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
		Long id,
		Long memberId,
		String status,
		BigDecimal totalAmount,
		List<OrderItemResponse> items,
		Instant createdAt) {

	public static OrderResponse from(Order order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(OrderItemResponse::from)
				.toList();
		return new OrderResponse(
				order.getId(),
				order.getMemberId(),
				order.getStatus().name(),
				order.getTotalAmount(),
				items,
				order.getCreatedAt());
	}

	public record OrderItemResponse(
			Long id,
			Long productId,
			String productName,
			BigDecimal unitPrice,
			int quantity,
			BigDecimal lineTotal) {

		public static OrderItemResponse from(OrderItem item) {
			return new OrderItemResponse(
					item.getId(),
					item.getProductId(),
					item.getProductName(),
					item.getUnitPrice(),
					item.getQuantity(),
					item.getLineTotal());
		}
	}
}
