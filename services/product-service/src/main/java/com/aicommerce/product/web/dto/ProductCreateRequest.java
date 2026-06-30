package com.aicommerce.product.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductCreateRequest(
		@NotBlank @Size(max = 255) String name,
		@Size(max = 1000) String description,
		@NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
		@NotNull @PositiveOrZero Integer stockQuantity) {
}
