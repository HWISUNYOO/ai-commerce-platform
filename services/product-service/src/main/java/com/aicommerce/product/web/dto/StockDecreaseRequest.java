package com.aicommerce.product.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** 여러 상품의 재고를 한 번에 차감 요청(주문 항목 단위). */
public record StockDecreaseRequest(@NotEmpty @Valid List<Item> items) {

	public record Item(@NotNull Long productId, @NotNull @Positive Integer quantity) {
	}
}
