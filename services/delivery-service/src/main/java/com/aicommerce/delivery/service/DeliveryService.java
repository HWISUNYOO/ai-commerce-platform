package com.aicommerce.delivery.service;

import com.aicommerce.delivery.domain.Delivery;
import com.aicommerce.delivery.exception.NotFoundException;
import com.aicommerce.delivery.repository.DeliveryRepository;
import com.aicommerce.delivery.web.dto.DeliveryCreateRequest;
import com.aicommerce.delivery.web.dto.DeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {

	private final DeliveryRepository deliveryRepository;

	@Transactional
	public DeliveryResponse create(DeliveryCreateRequest request) {
		Delivery delivery = Delivery.builder()
				.orderId(request.orderId())
				.recipientName(request.recipientName())
				.address(request.address())
				.build();
		return DeliveryResponse.from(deliveryRepository.save(delivery));
	}

	@Transactional(readOnly = true)
	public DeliveryResponse get(Long id) {
		return deliveryRepository.findById(id)
				.map(DeliveryResponse::from)
				.orElseThrow(() -> new NotFoundException("Delivery", id));
	}

	@Transactional(readOnly = true)
	public List<DeliveryResponse> list() {
		return deliveryRepository.findAll().stream()
				.map(DeliveryResponse::from)
				.toList();
	}
}
