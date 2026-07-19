package com.aicommerce.point.web;

import com.aicommerce.point.service.PointService;
import com.aicommerce.point.web.dto.PointBalanceResponse;
import com.aicommerce.point.web.dto.PointTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

	private final PointService pointService;

	/** 회원의 현재 포인트 잔액. */
	@GetMapping("/{memberId}")
	public PointBalanceResponse balance(@PathVariable Long memberId) {
		return new PointBalanceResponse(memberId, pointService.balance(memberId));
	}

	/** 회원의 포인트 적립/사용 내역(최근순). */
	@GetMapping("/{memberId}/history")
	public List<PointTransactionResponse> history(@PathVariable Long memberId) {
		return pointService.history(memberId).stream()
				.map(PointTransactionResponse::from)
				.toList();
	}
}
