package com.aicommerce.point.repository;

import com.aicommerce.point.domain.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

	boolean existsByOrderId(Long orderId);

	List<PointTransaction> findByMemberIdOrderByIdDesc(Long memberId);

	@Query("select coalesce(sum(t.amount), 0) from PointTransaction t where t.memberId = :memberId")
	long sumAmountByMemberId(@Param("memberId") Long memberId);
}
