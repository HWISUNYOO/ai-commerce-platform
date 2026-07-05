package com.aicommerce.product.service;

import com.aicommerce.product.domain.Product;
import com.aicommerce.product.exception.NotFoundException;
import com.aicommerce.product.repository.ProductRepository;
import com.aicommerce.product.web.dto.ProductCreateRequest;
import com.aicommerce.product.web.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
