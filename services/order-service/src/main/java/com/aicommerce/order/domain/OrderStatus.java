package com.aicommerce.order.domain;

public enum OrderStatus {
	PENDING,    // 주문 접수, 재고예약/결제 대기 (Saga 시작)
	CONFIRMED,  // 결제 승인까지 완료 (Saga 성공)
	CANCELLED,  // 재고부족/결제실패로 취소 (Saga 보상)
	// --- 이하 레거시 값(구 데이터 호환용) ---
	CREATED,
	PAID,
	SHIPPED
}
