package com.aicommerce.order.client;

import com.aicommerce.order.exception.InsufficientStockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * product-service의 재고 차감 API를 호출하는 클라이언트. 재고는 product-service가 소유하므로
 * (Database per Service) 주문 시 REST로 원자적 차감을 위임한다. 재고 부족(409)은 주문 거부로 변환한다.
 */
@Component
public class ProductStockClient {

	private final RestClient rest;
	private final ObjectMapper objectMapper;

	public ProductStockClient(RestClient.Builder builder, ObjectMapper objectMapper,
			@Value("${services.product-url:http://localhost:8082}") String baseUrl) {
		this.rest = builder.baseUrl(baseUrl).build();
		this.objectMapper = objectMapper;
	}

	public void decreaseStock(List<Item> items) {
		rest.post()
				.uri("/api/products/stock/decrease")
				.contentType(MediaType.APPLICATION_JSON)
				.body(new StockDecreaseRequest(items))
				.retrieve()
				.onStatus(status -> status.value() == HttpStatus.CONFLICT.value(),
						(req, res) -> throwInsufficientStock(res.getBody().readAllBytes()))
				.toBodilessEntity();
	}

	private void throwInsufficientStock(byte[] body) {
		String message = "재고가 부족합니다.";
		try {
			var node = objectMapper.readTree(new String(body, StandardCharsets.UTF_8));
			if (node.hasNonNull("message")) {
				message = node.get("message").asText();
			}
		}
		catch (IOException ignored) {
			// 본문 파싱 실패 시 기본 메시지 사용
		}
		throw new InsufficientStockException(message);
	}

	public record Item(Long productId, int quantity) {
	}

	public record StockDecreaseRequest(List<Item> items) {
	}
}
