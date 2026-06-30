package com.aicommerce.payment.service;

import com.aicommerce.payment.domain.Payment;
import com.aicommerce.payment.exception.NotFoundException;
import com.aicommerce.payment.repository.PaymentRepository;
import com.aicommerce.payment.web.dto.PaymentCreateRequest;
import com.aicommerce.payment.web.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;

	@Transactional
	public PaymentResponse create(PaymentCreateRequest request) {
		Payment payment = Payment.builder()
				.orderId(request.orderId())
				.amount(request.amount())
				.method(request.method())
				.build();
		return PaymentResponse.from(paymentRepository.save(payment));
	}

	@Transactional(readOnly = true)
	public PaymentResponse get(Long id) {
		return paymentRepository.findById(id)
				.map(PaymentResponse::from)
				.orElseThrow(() -> new NotFoundException("Payment", id));
	}

	@Transactional(readOnly = true)
	public List<PaymentResponse> list() {
		return paymentRepository.findAll().stream()
				.map(PaymentResponse::from)
				.toList();
	}
}
