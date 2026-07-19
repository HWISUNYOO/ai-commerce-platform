package com.aicommerce.product.service;

import com.aicommerce.product.domain.Product;
import com.aicommerce.product.domain.StockReservation;
import com.aicommerce.product.domain.StockReservationStatus;
import com.aicommerce.product.event.OrderCreatedEvent;
import com.aicommerce.product.exception.InsufficientStockException;
import com.aicommerce.product.exception.NotFoundException;
import com.aicommerce.product.repository.ProductRepository;
import com.aicommerce.product.repository.StockReservationRepository;
import com.aicommerce.product.web.dto.ProductCreateRequest;
import com.aicommerce.product.web.dto.ProductResponse;
import com.aicommerce.product.web.dto.StockDecreaseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final StockReservationRepository reservationRepository;

	@Transactional
	public ProductResponse create(ProductCreateRequest request) {
		Product product = Product.builder()
				.name(request.name())
				.description(request.description())
				.price(request.price())
				.stockQuantity(request.stockQuantity())
				.build();
		return ProductResponse.from(productRepository.save(product));
	}

	@Cacheable(cacheNames = "products", key = "#id")
	@Transactional(readOnly = true)
	public ProductResponse get(Long id) {
		return productRepository.findById(id)
				.map(ProductResponse::from)
				.orElseThrow(() -> new NotFoundException("Product", id));
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> list() {
		return productRepository.findAll().stream()
				.map(ProductResponse::from)
				.toList();
	}

	/**
	 * 주문 항목만큼 재고를 차감한다. 한 항목이라도 부족하면 전체를 롤백(같은 트랜잭션)해 부분 차감을 막는다.
	 * 재고가 바뀌므로 캐시된 상품(구 재고)을 모두 무효화한다.
	 */
	@CacheEvict(cacheNames = "products", allEntries = true)
	@Transactional
	public void decreaseStock(List<StockDecreaseRequest.Item> items) {
		Instant now = Instant.now();
		for (StockDecreaseRequest.Item item : items) {
			int updated = productRepository.decreaseStock(item.productId(), item.quantity(), now);
			if (updated == 0) {
				if (!productRepository.existsById(item.productId())) {
					throw new NotFoundException("Product", item.productId());
				}
				throw new InsufficientStockException(item.productId());
			}
		}
	}

	/**
	 * 주문(Saga)의 재고를 예약한다. 한 항목이라도 부족하면 예외를 던져 트랜잭션 전체를 롤백하므로
	 * 부분 차감이 남지 않는다. 예약 성공 시 항목별 예약 기록을 남겨 이후 보상(복원)에 사용한다.
	 * 같은 주문이 중복 전달되면(order_id 예약 존재) 재차감 없이 성공으로 간주한다(멱등).
	 */
	@CacheEvict(cacheNames = "products", allEntries = true)
	@Transactional
	public void reserveForOrder(Long orderId, List<OrderCreatedEvent.Item> items) {
		if (reservationRepository.existsByOrderId(orderId)) {
			return; // 이미 예약된 주문 → 멱등 처리
		}
		Instant now = Instant.now();
		for (OrderCreatedEvent.Item item : items) {
			int updated = productRepository.decreaseStock(item.productId(), item.quantity(), now);
			if (updated == 0) {
				if (!productRepository.existsById(item.productId())) {
					throw new NotFoundException("Product", item.productId());
				}
				throw new InsufficientStockException(item.productId());
			}
			reservationRepository.save(StockReservation.builder()
					.orderId(orderId)
					.productId(item.productId())
					.quantity(item.quantity())
					.build());
		}
	}

	/**
	 * 주문의 예약 재고를 복원한다(Saga 보상). 결제 실패/취소 시 호출된다.
	 * 이미 복원된 예약은 건너뛰므로 중복 이벤트에도 안전하다(멱등).
	 */
	@CacheEvict(cacheNames = "products", allEntries = true)
	@Transactional
	public void releaseForOrder(Long orderId) {
		List<StockReservation> reservations =
				reservationRepository.findByOrderIdAndStatus(orderId, StockReservationStatus.RESERVED);
		Instant now = Instant.now();
		for (StockReservation r : reservations) {
			productRepository.increaseStock(r.getProductId(), r.getQuantity(), now);
			r.release();
		}
	}
}
