package com.aicommerce.notification.service;

import com.aicommerce.notification.domain.Notification;
import com.aicommerce.notification.domain.NotificationType;
import com.aicommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRepository repository;

	/** 알림을 저장하고 발송한다(현재 발송은 로그 출력 mock). */
	@Transactional
	public Notification send(Long memberId, NotificationType type, Long referenceId, String message) {
		Notification saved = repository.save(Notification.builder()
				.memberId(memberId)
				.type(type)
				.referenceId(referenceId)
				.message(message)
				.build());
		// mock 발송: 실제로는 EMAIL/SMS/PUSH 게이트웨이 호출
		log.info("[NOTIFY:{}] member={} : {}", type, memberId, message);
		return saved;
	}

	@Transactional(readOnly = true)
	public List<Notification> list(Long memberId) {
		return repository.findByMemberIdOrderByIdDesc(memberId);
	}
}
