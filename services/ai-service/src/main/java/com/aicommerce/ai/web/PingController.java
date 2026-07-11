package com.aicommerce.ai.web;

import com.aicommerce.ai.llm.LlmGateway;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LLM 연동 스모크 테스트 엔드포인트: 프롬프트를 활성 {@link LlmGateway}(기본 구독 CLI)로 넘겨 응답을 돌려준다.
 * 실제 AI 기능(RAG 추천 등)은 {@code /api/ai/*}에 있다.
 *
 * <pre>
 *   curl -X POST localhost:8080/api/ping \
 *        -H "Content-Type: application/json" \
 *        -d '{"prompt":"Say hello from the ai-service in one sentence."}'
 * </pre>
 */
@RestController
@RequestMapping("/api")
public class PingController {

	private final LlmGateway llm;

	public PingController(LlmGateway llm) {
		this.llm = llm;
	}

	@PostMapping("/ping")
	public PingResponse ping(@RequestBody @jakarta.validation.Valid PingRequest request) {
		String reply = llm.complete(request.prompt());
		return new PingResponse(reply);
	}

	public record PingRequest(@NotBlank String prompt) {
	}

	public record PingResponse(String reply) {
	}
}
