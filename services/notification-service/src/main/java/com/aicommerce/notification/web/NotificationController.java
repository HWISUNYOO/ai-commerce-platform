package com.aicommerce.notification.web;

import com.aicommerce.notification.service.NotificationService;
import com.aicommerce.notification.web.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	/** 회원의 알림 목록(최근순). */
	@GetMapping
	public List<NotificationResponse> list(@RequestParam Long memberId) {
		return notificationService.list(memberId).stream()
				.map(NotificationResponse::from)
				.toList();
	}
}
