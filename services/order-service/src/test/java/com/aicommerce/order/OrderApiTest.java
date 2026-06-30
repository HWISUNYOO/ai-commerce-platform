package com.aicommerce.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
