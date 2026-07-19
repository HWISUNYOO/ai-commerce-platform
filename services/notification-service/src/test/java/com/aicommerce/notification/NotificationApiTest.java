package com.aicommerce.notification;

import com.aicommerce.notification.domain.NotificationType;
import com.aicommerce.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationApiTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private NotificationService notificationService;

	@Test
	void savesAndListsNotificationsByMember() throws Exception {
		notificationService.send(1L, NotificationType.ORDER_CREATED, 1001L, "주문이 접수되었습니다. (주문번호 1001)");
		notificationService.send(1L, NotificationType.PAYMENT_APPROVED, 1001L, "결제가 완료되었습니다. 85000원 (주문번호 1001)");
		notificationService.send(2L, NotificationType.ORDER_CREATED, 1002L, "주문이 접수되었습니다. (주문번호 1002)");

		mvc.perform(get("/api/notifications").param("memberId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].type").value("PAYMENT_APPROVED")); // 최근순

		mvc.perform(get("/api/notifications").param("memberId", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}
}
