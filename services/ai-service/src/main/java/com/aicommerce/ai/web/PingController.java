package com.aicommerce.ai.web;

import com.aicommerce.ai.claude.ClaudeClient;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 0 proof-of-concept endpoint: forwards a prompt to the Claude API and returns the reply.
 * Kept as a smoke test for the LLM integration; real AI features (RAG, shopping agent) arrive in Phase 4.
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

	private final ClaudeClient claude;

	public PingController(ClaudeClient claude) {
		this.claude = claude;
	}

	@PostMapping("/ping")
	public PingResponse ping(@RequestBody @jakarta.validation.Valid PingRequest request) {
		String reply = claude.complete(request.prompt());
		return new PingResponse(reply);
	}

	public record PingRequest(@NotBlank String prompt) {
	}

	public record PingResponse(String reply) {
	}
}
