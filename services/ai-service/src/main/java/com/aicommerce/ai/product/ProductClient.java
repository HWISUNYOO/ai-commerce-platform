package com.aicommerce.ai.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * product-service를 호출해 상품 카탈로그를 가져오는 클라이언트(RAG의 Retrieval 단계).
 * 서비스 간 통신은 REST로 하며, 대상 주소는 환경변수/설정으로 주입한다(Docker/K8s에서 교체 가능).
 */
@Component
public class ProductClient {

	private final RestClient rest;

	public ProductClient(RestClient.Builder builder,
			@Value("${services.product-url:http://localhost:8082}") String baseUrl) {
		this.rest = builder.baseUrl(baseUrl).build();
	}

	/** 전체 상품 목록을 조회한다. */
	public List<ProductView> findAll() {
		return rest.get()
				.uri("/api/products")
				.retrieve()
				.body(new ParameterizedTypeReference<List<ProductView>>() {
				});
	}
}
