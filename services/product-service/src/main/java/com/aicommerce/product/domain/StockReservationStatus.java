package com.aicommerce.product.domain;

public enum StockReservationStatus {
	RESERVED,  // 재고 차감(예약)됨
	RELEASED   // 보상으로 재고 복원됨
}
