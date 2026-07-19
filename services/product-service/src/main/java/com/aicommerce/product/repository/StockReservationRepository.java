package com.aicommerce.product.repository;

import com.aicommerce.product.domain.StockReservation;
import com.aicommerce.product.domain.StockReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

	boolean existsByOrderId(Long orderId);

	List<StockReservation> findByOrderIdAndStatus(Long orderId, StockReservationStatus status);
}
