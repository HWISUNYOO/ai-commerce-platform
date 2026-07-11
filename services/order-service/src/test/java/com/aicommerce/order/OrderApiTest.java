package com.aicommerce.order;

import com.aicommerce.order.client.ProductStockClient;
import com.aicommerce.order.exception.InsufficientStockException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	// 재고 차감은 product-service 호출이므로 테스트에선 대체(기본 동작=아무것도 안 함=재고 충분).
	@MockitoBean
	private ProductStockClient productStockClient;

	@Test
	void createAndGetOrder() throws Exception {
		String body = """
				{
				  "memberId": 1,
				  "items": [
				    {"productId": 10, "productName": "Item A", "unitPrice": 1000, "quantity": 2},
				    {"productId": 11, "productName": "Item B", "unitPrice": 500, "quantity": 3}
				  ]
				}
				""";

		MvcResult created = mvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("CREATED"))
				.andExpect(jsonPath("$.totalAmount").value(3500))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andReturn();

		JsonNode node = objectMapper.readTree(created.getResponse().getContentAsString());
		long id = node.get("id").asLong();

		mvc.perform(get("/api/orders/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.memberId").value(1))
				.andExpect(jsonPath("$.items[0].lineTotal").value(2000));
	}

	@Test
	void rejectsOrderWhenStockInsufficient() throws Exception {
		doThrow(new InsufficientStockException("상품 10의 재고가 부족합니다."))
				.when(productStockClient).decreaseStock(anyList());

		String body = """
				{"memberId": 1, "items": [
				  {"productId": 10, "productName": "Item A", "unitPrice": 1000, "quantity": 999}
				]}
				""";

		mvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
	}

	@Test
	void rejectsOrderWithNoItems() throws Exception {
		String body = """
				{"memberId": 1, "items": []}
				""";

		mvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	void returns404ForMissingOrder() throws Exception {
		mvc.perform(get("/api/orders/{id}", 999999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}
}
