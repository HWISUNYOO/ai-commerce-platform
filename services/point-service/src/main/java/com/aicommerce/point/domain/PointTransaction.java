package com.aicommerce.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 포인트 적립/사용 원장(ledger). 잔액은 회원별 amount 합으로 계산한다.
 * order_id 에 UNIQUE 제약을 걸어 주문당 1회만 적립되도록 DB 레벨에서 멱등성을 보장한다.
 */
@Entity
@Table(name = "point_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "order_id", nullable = false, unique = true)
	private Long orderId;

	@Column(nullable = false)
	private long amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PointTransactionType type;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Builder
	private PointTransaction(Long memberId, Long orderId, long amount, PointTransactionType type) {
		this.memberId = memberId;
		this.orderId = orderId;
		this.amount = amount;
		this.type = type;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}
}
