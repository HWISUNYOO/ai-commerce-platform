package com.aicommerce.order.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreateRequest(
		@NotNull Long memberId,
		@NotEmpty @Valid List<OrderItemRequest> items) {

	public record OrderItemRequest(
			@NotNull Long productId,
			@NotBlank String productName,
			@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal unitPrice,
			@NotNull @Positive Integer quantity) {
	}
}
