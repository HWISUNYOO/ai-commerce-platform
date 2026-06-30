package com.aicommerce.delivery;

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
class DeliveryApiTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createAndGetDelivery() throws Exception {
		String body = """
				{"orderId":1,"recipientName":"Alice","address":"Seoul Jung-gu Toegye-ro 1"}
				""";

		MvcResult created = mvc.perform(post("/api/deliveries")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.orderId").value(1))
				.andExpect(jsonPath("$.status").value("PREPARING"))
				.andReturn();

		JsonNode node = objectMapper.readTree(created.getResponse().getContentAsString());
		long id = node.get("id").asLong();

		mvc.perform(get("/api/deliveries/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.recipientName").value("Alice"));
	}

	@Test
	void rejectsInvalidRequest() throws Exception {
		String body = """
				{"orderId":1,"recipientName":"","address":"Seoul Jung-gu Toegye-ro 1"}
				""";

		mvc.perform(post("/api/deliveries")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	void returns404ForMissingDelivery() throws Exception {
		mvc.perform(get("/api/deliveries/{id}", 999999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}
}
