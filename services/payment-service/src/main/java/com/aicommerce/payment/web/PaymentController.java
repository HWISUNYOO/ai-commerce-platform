package com.aicommerce.payment.web;

import com.aicommerce.payment.service.PaymentService;
import com.aicommerce.payment.web.dto.PaymentCreateRequest;
import com.aicommerce.payment.web.dto.PaymentResponse;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PaymentResponse create(@RequestBody @Valid PaymentCreateRequest request) {
		return paymentService.create(request);
	}

	@GetMapping("/{id}")
	public PaymentResponse get(@PathVariable Long id) {
		return paymentService.get(id);
	}

	@GetMapping
	public List<PaymentResponse> list() {
		return paymentService.list();
	}
}
