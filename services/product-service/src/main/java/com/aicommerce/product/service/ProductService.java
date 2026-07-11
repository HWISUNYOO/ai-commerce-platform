package com.aicommerce.product.service;

import com.aicommerce.product.domain.Product;
import com.aicommerce.product.exception.InsufficientStockException;
import com.aicommerce.product.exception.NotFoundException;
import com.aicommerce.product.repository.ProductRepository;
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
}
