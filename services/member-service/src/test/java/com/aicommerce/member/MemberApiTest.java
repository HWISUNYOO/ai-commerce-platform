package com.aicommerce.member;

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
class MemberApiTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createAndGetMember() throws Exception {
		String body = """
				{"email":"alice@example.com","password":"password123","name":"Alice"}
				""";

		MvcResult created = mvc.perform(post("/api/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("alice@example.com"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andReturn();

		JsonNode node = objectMapper.readTree(created.getResponse().getContentAsString());
		long id = node.get("id").asLong();

		mvc.perform(get("/api/members/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Alice"));
	}

	@Test
	void rejectsDuplicateEmail() throws Exception {
		String body = """
				{"email":"dup@example.com","password":"password123","name":"Bob"}
				""";

		mvc.perform(post("/api/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());

		mvc.perform(post("/api/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CONFLICT"));
	}

	@Test
	void rejectsInvalidEmail() throws Exception {
		String body = """
				{"email":"not-an-email","password":"password123","name":"Bob"}
				""";

		mvc.perform(post("/api/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	void returns404ForMissingMember() throws Exception {
		mvc.perform(get("/api/members/{id}", 999999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}
}
