package com.aicommerce.delivery.web;

import com.aicommerce.delivery.service.DeliveryService;
import com.aicommerce.delivery.web.dto.DeliveryCreateRequest;
import com.aicommerce.delivery.web.dto.DeliveryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

	private final DeliveryService deliveryService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public DeliveryResponse create(@RequestBody @Valid DeliveryCreateRequest request) {
		return deliveryService.create(request);
	}

	@GetMapping("/{id}")
	public DeliveryResponse get(@PathVariable Long id) {
		return deliveryService.get(id);
	}

	@GetMapping
	public List<DeliveryResponse> list() {
		return deliveryService.list();
	}
}
