package com.aicommerce.ai.llm.api;

import com.aicommerce.ai.claude.ClaudeClient;
import com.aicommerce.ai.llm.LlmGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Anthropic Messages API 백엔드(토큰 과금). {@code llm.backend=api}일 때만 활성화된다.
 * 실제 HTTP 호출은 기존 {@link ClaudeClient}에 위임한다.
 */
@Component
@ConditionalOnProperty(name = "llm.backend", havingValue = "api")
public class ClaudeApiGateway implements LlmGateway {

	private final ClaudeClient client;

	public ClaudeApiGateway(ClaudeClient client) {
		this.client = client;
	}

	@Override
	public String complete(String userPrompt, String systemPrompt) {
		return client.complete(userPrompt, systemPrompt);
	}

	@Override
	public String backendName() {
		return "claude-api";
	}
}
