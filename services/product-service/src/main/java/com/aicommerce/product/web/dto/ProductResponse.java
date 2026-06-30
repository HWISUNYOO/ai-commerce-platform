package com.aicommerce.product.web.dto;

import com.aicommerce.product.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
		Long id,
		String name,
		String description,
		BigDecimal price,
		Integer stockQuantity,
		String status,
		Instant createdAt) {

	public static ProductResponse from(Product p) {
		return new ProductResponse(
				p.getId(),
				p.getName(),
				p.getDescription(),
				p.getPrice(),
				p.getStockQuantity(),
				p.getStatus().name(),
				p.getCreatedAt());
	}
}
