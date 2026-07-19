package com.aicommerce.point;

import com.aicommerce.point.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PointApiTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PointService pointService;

	private static final BigDecimal RATE = new BigDecimal("0.01");

	@Test
	void earnsOnePercentAndIsIdempotentPerOrder() throws Exception {
		// 85,000원 결제 -> 850P 적립
		pointService.earn(1L, 1001L, new BigDecimal("85000"), RATE);
		// 같은 주문(order_id=1001)이 중복 전달돼도 재적립되지 않는다.
		pointService.earn(1L, 1001L, new BigDecimal("85000"), RATE);

		mvc.perform(get("/api/points/{memberId}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.memberId").value(1))
				.andExpect(jsonPath("$.balance").value(850));
	}

	@Test
	void accumulatesBalanceAcrossOrders() throws Exception {
		pointService.earn(2L, 2001L, new BigDecimal("50000"), RATE); // 500P
		pointService.earn(2L, 2002L, new BigDecimal("30000"), RATE); // 300P

		mvc.perform(get("/api/points/{memberId}", 2L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.balance").value(800));

		mvc.perform(get("/api/points/{memberId}/history", 2L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}
}
