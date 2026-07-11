package com.aicommerce.ai.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * product-service 응답에서 추천에 필요한 필드만 담는 뷰. 나머지 필드(createdAt 등)는 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductView(
		Long id,
		String name,
		String description,
		BigDecimal price,
		Integer stockQuantity,
		String status) {
}
