package com.aicommerce.product.repository;

import com.aicommerce.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ProductRepository extends JpaRepository<Product, Long> {

	/**
	 * 재고를 원자적으로 차감한다. {@code stock_quantity >= qty} 조건을 WHERE에 두어, 재고가 부족하면
	 * 0행이 갱신되고(차감 안 됨) 충분하면 1행이 갱신된다. 단일 SQL이라 동시 주문에도 경쟁 상태가 없다.
	 *
	 * @return 갱신된 행 수(1=성공, 0=재고 부족 또는 없는 상품)
	 */
	@Modifying
	@Query("update Product p set p.stockQuantity = p.stockQuantity - :qty, p.updatedAt = :now "
			+ "where p.id = :id and p.stockQuantity >= :qty")
	int decreaseStock(@Param("id") Long id, @Param("qty") int qty, @Param("now") Instant now);

	/** 재고를 되돌린다(Saga 보상). 결제 실패/취소 시 예약했던 수량을 복원한다. */
	@Modifying
	@Query("update Product p set p.stockQuantity = p.stockQuantity + :qty, p.updatedAt = :now "
			+ "where p.id = :id")
	int increaseStock(@Param("id") Long id, @Param("qty") int qty, @Param("now") Instant now);
}
