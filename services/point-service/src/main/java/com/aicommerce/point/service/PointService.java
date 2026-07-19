package com.aicommerce.point.service;

import com.aicommerce.point.domain.PointTransaction;
import com.aicommerce.point.domain.PointTransactionType;
import com.aicommerce.point.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointTransactionRepository repository;

	/**
	 * 결제금액 × 적립률로 포인트를 적립한다.
	 * 이미 적립된 주문(order_id 존재)이면 아무 것도 하지 않고 빈 값을 반환한다(멱등).
	 */
	@Transactional
	public Optional<PointTransaction> earn(Long memberId, Long orderId, BigDecimal paymentAmount, BigDecimal rate) {
		if (repository.existsByOrderId(orderId)) {
			return Optional.empty();
		}
		long points = paymentAmount.multiply(rate).setScale(0, RoundingMode.DOWN).longValueExact();
		PointTransaction tx = repository.save(PointTransaction.builder()
				.memberId(memberId)
				.orderId(orderId)
				.amount(points)
				.type(PointTransactionType.EARN)
				.build());
		return Optional.of(tx);
	}

	@Transactional(readOnly = true)
	public long balance(Long memberId) {
		return repository.sumAmountByMemberId(memberId);
	}

	@Transactional(readOnly = true)
	public List<PointTransaction> history(Long memberId) {
		return repository.findByMemberIdOrderByIdDesc(memberId);
	}
}
