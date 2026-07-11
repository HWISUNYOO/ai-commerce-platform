package com.aicommerce.product.web;

import com.aicommerce.product.service.ProductService;
import com.aicommerce.product.web.dto.ProductCreateRequest;
import com.aicommerce.product.web.dto.ProductResponse;
import com.aicommerce.product.web.dto.StockDecreaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponse create(@RequestBody @Valid ProductCreateRequest request) {
		return productService.create(request);
	}

	@GetMapping("/{id}")
	public ProductResponse get(@PathVariable Long id) {
		return productService.get(id);
	}

	@GetMapping
	public List<ProductResponse> list() {
		return productService.list();
	}

	/** 주문 시 재고 차감(order-service가 호출). 재고 부족이면 409를 반환한다. */
	@PostMapping("/stock/decrease")
	@ResponseStatus(HttpStatus.OK)
	public void decreaseStock(@RequestBody @Valid StockDecreaseRequest request) {
		productService.decreaseStock(request.items());
	}
}
