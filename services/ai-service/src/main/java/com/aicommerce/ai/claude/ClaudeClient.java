package com.aicommerce.ai.claude;

import com.aicommerce.ai.claude.ClaudeMessages.Message;
import com.aicommerce.ai.claude.ClaudeMessages.MessageRequest;
import com.aicommerce.ai.claude.ClaudeMessages.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Thin synchronous client for the Anthropic Messages API, built on Spring's {@link RestClient}.
 *
 * <p>Opt-in LLM backend (per ADR-0005). The default backend is the subscription-based Claude
 * Code CLI; this client is used only when an API key is supplied. Later phases add tool use,
 * prompt caching, streaming, and structured-output parsing on top of this.
 */
@Component
public class ClaudeClient {

	private static final Logger log = LoggerFactory.getLogger(ClaudeClient.class);

	private final RestClient restClient;
	private final ClaudeProperties props;

	public ClaudeClient(ClaudeProperties props, RestClient.Builder builder) {
		this.props = props;
		this.restClient = builder
				.baseUrl(props.baseUrl())
				.defaultHeader("x-api-key", props.apiKey())
				.defaultHeader("anthropic-version", props.anthropicVersion())
				.build();
	}

	/** Send a single user prompt and return Claude's text reply. */
	public String complete(String userPrompt) {
		return complete(userPrompt, null);
	}

	/** Send a user prompt with an optional system prompt and return Claude's text reply. */
	public String complete(String userPrompt, String systemPrompt) {
		if (!props.hasApiKey()) {
			throw new IllegalStateException(
					"ANTHROPIC_API_KEY is not set. Export it before calling the Claude API.");
		}

		MessageRequest request = new MessageRequest(
				props.model(),
				props.maxTokens(),
				List.of(Message.user(userPrompt)),
				systemPrompt);

		MessageResponse response = restClient.post()
				.uri("/v1/messages")
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(MessageResponse.class);

		if (response == null) {
			throw new IllegalStateException("Empty response from Claude API");
		}

		if (response.usage() != null) {
			log.info("Claude call ok: model={}, in={}, out={}, stop={}",
					response.model(),
					response.usage().inputTokens(),
					response.usage().outputTokens(),
					response.stopReason());
		}

		return response.text();
	}
}
