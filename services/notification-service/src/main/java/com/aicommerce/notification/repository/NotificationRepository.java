package com.aicommerce.notification.repository;

import com.aicommerce.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByMemberIdOrderByIdDesc(Long memberId);
}
