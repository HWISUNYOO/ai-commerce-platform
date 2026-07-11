package com.aicommerce.order.exception;

/** 재고 부족으로 주문을 진행할 수 없을 때 발생(product-service가 409로 응답). */
public class InsufficientStockException extends RuntimeException {

	public InsufficientStockException(String message) {
		super(message);
	}
}
