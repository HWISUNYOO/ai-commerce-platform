package com.aicommerce.ai.agent.tool;

import com.aicommerce.ai.product.ProductClient;
import com.aicommerce.ai.product.ProductView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 키워드/최대가격으로 카탈로그를 검색하는 도구. product-service에서 상품을 가져와 조건으로 필터링한다.
 *
 * <p>지금은 전체를 받아 in-memory 필터링한다(상품 수가 적음). 상품이 많아지면 product-service에
 * 검색 엔드포인트(또는 벡터 검색)를 두고 여기서 위임하는 것이 확장에 맞다.
 */
@Component
public class SearchProductsTool implements AgentTool {

	private static final int MAX_RESULTS = 20;

	private final ProductClient productClient;
	private final ObjectMapper mapper;

	public SearchProductsTool(ProductClient productClient, ObjectMapper mapper) {
		this.productClient = productClient;
		this.mapper = mapper;
	}

	@Override
	public String name() {
		return "search_products";
	}

	@Override
	public String description() {
		return "search_products(keyword: 문자열, max_price: 숫자) — 키워드가 이름/설명에 포함되고 "
				+ "가격이 max_price 이하인 상품을 반환. 두 인자 모두 생략 가능(생략 시 전체).";
	}

	@Override
	public String execute(JsonNode args) {
		String keyword = text(args, "keyword");
		BigDecimal maxPrice = number(args, "max_price");

		List<ProductView> matched = productClient.findAll().stream()
				.filter(p -> "ACTIVE".equalsIgnoreCase(p.status()))
				.filter(p -> keyword == null || matchesKeyword(p, keyword))
				.filter(p -> maxPrice == null || p.price().compareTo(maxPrice) <= 0)
				.limit(MAX_RESULTS)
				.toList();

		try {
			return mapper.writeValueAsString(matched);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to serialize search results", e);
		}
	}

	private boolean matchesKeyword(ProductView p, String keyword) {
		String k = keyword.toLowerCase();
		String name = p.name() == null ? "" : p.name().toLowerCase();
		String desc = p.description() == null ? "" : p.description().toLowerCase();
		return name.contains(k) || desc.contains(k);
	}

	private String text(JsonNode args, String field) {
		JsonNode n = args.get(field);
		if (n == null || n.isNull()) {
			return null;
		}
		String v = n.asText().trim();
		return v.isEmpty() ? null : v;
	}

	private BigDecimal number(JsonNode args, String field) {
		JsonNode n = args.get(field);
		if (n == null || n.isNull() || n.asText().isBlank()) {
			return null;
		}
		try {
			return new BigDecimal(n.asText().trim());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
}
