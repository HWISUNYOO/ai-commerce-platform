package com.aicommerce.delivery.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeliveryCreateRequest(
		@NotNull Long orderId,
		@NotBlank @Size(max = 100) String recipientName,
		@NotBlank @Size(max = 500) String address) {
}
