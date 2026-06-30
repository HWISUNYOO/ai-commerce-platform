package com.aicommerce.payment.web.dto;

import com.aicommerce.payment.domain.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentCreateRequest(
		@NotNull Long orderId,
		@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
		@NotNull PaymentMethod method) {
}
