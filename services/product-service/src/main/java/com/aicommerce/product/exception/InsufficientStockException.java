package com.aicommerce.product.exception;

/** 요청 수량보다 재고가 부족할 때 발생. */
public class InsufficientStockException extends RuntimeException {

	public InsufficientStockException(Long productId) {
		super("상품 " + productId + "의 재고가 부족합니다.");
	}
}
