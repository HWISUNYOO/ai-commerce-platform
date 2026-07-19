package com.aicommerce.order;

import com.aicommerce.order.service.OrderService;
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

	@Autowired
	private OrderService orderService;

	private long createOrder() throws Exception {
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
				.andReturn();
		return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
	}

	@Test
	void createsOrderAsPending() throws Exception {
		// Saga 시작: 주문은 PENDING 으로 생성되고 재고예약/결제는 이벤트로 비동기 처리된다.
		long id = createOrder();

		mvc.perform(get("/api/orders/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.totalAmount").value(3500))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].lineTotal").value(2000));
	}

	@Test
	void paymentApprovedConfirmsOrder() throws Exception {
		long id = createOrder();
		orderService.confirm(id); // payment.approved 수신 시 호출되는 경로

		mvc.perform(get("/api/orders/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMED"));
	}

	@Test
	void stockRejectedOrPaymentFailedCancelsOrder() throws Exception {
		long id = createOrder();
		orderService.cancel(id); // stock.rejected / payment.failed 수신 시 호출되는 보상 경로

		mvc.perform(get("/api/orders/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));
	}

	@Test
	void confirmIsIgnoredAfterCancel() throws Exception {
		// 상태 전이는 PENDING 일 때만 → 취소된 주문이 뒤늦은 승인 이벤트로 되살아나지 않는다.
		long id = createOrder();
		orderService.cancel(id);
		orderService.confirm(id);

		mvc.perform(get("/api/orders/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));
	}

	@Test
	void rejectsOrderWithNoItems() throws Exception {
		mvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"memberId": 1, "items": []}
								"""))
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
