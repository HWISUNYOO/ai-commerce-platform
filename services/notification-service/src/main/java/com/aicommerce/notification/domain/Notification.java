package com.aicommerce.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 발송된 알림 기록. 실제 발송은 mock(로그 출력)이며 채널은 LOG 로 저장한다.
 * 운영에서는 channel 을 EMAIL/SMS/PUSH 로 확장하고 status 로 발송 결과를 추적한다.
 */
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private NotificationType type;

	@Column(name = "reference_id")
	private Long referenceId;

	@Column(nullable = false, length = 500)
	private String message;

	@Column(nullable = false, length = 20)
	private String channel;

	@Column(nullable = false, length = 20)
	private String status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Builder
	private Notification(Long memberId, NotificationType type, Long referenceId, String message) {
		this.memberId = memberId;
		this.type = type;
		this.referenceId = referenceId;
		this.message = message;
		this.channel = "LOG";
		this.status = "SENT";
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}
}
