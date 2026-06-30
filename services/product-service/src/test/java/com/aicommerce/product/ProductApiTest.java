package com.aicommerce.product;

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
class ProductApiTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createAndGetProduct() throws Exception {
		String body = """
				{"name":"Widget","description":"A useful widget","price":19.99,"stockQuantity":10}
				""";

		MvcResult created = mvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Widget"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andReturn();

		JsonNode node = objectMapper.readTree(created.getResponse().getContentAsString());
		long id = node.get("id").asLong();

		mvc.perform(get("/api/products/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Widget"));
	}

	@Test
	void rejectsBlankName() throws Exception {
		String body = """
				{"name":"","description":"No name","price":19.99,"stockQuantity":10}
				""";

		mvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	void returns404ForMissingProduct() throws Exception {
		mvc.perform(get("/api/products/{id}", 999999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}
}
