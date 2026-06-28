package com.aicommerce.ai.claude;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Minimal record models for the Anthropic Messages API
 * (POST /v1/messages). Only the fields the service needs are mapped;
 * unknown response fields are ignored so the API can evolve without breaking us.
 */
public final class ClaudeMessages {

	private ClaudeMessages() {
	}

	/** A single conversation turn. {@code role} is "user" or "assistant". */
	public record Message(String role, String content) {

		public static Message user(String content) {
			return new Message("user", content);
		}
	}

	/** Request body for POST /v1/messages. */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record MessageRequest(
			String model,
			@JsonProperty("max_tokens") int maxTokens,
			List<Message> messages,
			String system) {
	}

	/** One block in the response {@code content} array. */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ContentBlock(String type, String text) {
	}

	/** Token accounting returned by the API. */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Usage(
			@JsonProperty("input_tokens") int inputTokens,
			@JsonProperty("output_tokens") int outputTokens) {
	}

	/** Response body from POST /v1/messages. */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record MessageResponse(
			String id,
			String model,
			@JsonProperty("stop_reason") String stopReason,
			List<ContentBlock> content,
			Usage usage) {

		/** Concatenate all text blocks (the API may return more than one). */
		public String text() {
			if (content == null) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			for (ContentBlock block : content) {
				if ("text".equals(block.type()) && block.text() != null) {
					sb.append(block.text());
				}
			}
			return sb.toString();
		}
	}
}
