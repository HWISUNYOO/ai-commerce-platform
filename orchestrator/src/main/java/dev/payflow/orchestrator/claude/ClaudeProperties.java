package dev.payflow.orchestrator.claude;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the Anthropic Claude API client.
 *
 * <p>The API key is read from the {@code ANTHROPIC_API_KEY} environment variable by default
 * (see application.yml). This key is billed per-token on the Anthropic Developer Platform and
 * is separate from any Claude.ai / Claude Code subscription.
 */
@ConfigurationProperties(prefix = "claude")
public record ClaudeProperties(
		@DefaultValue("") String apiKey,
		@DefaultValue("https://api.anthropic.com") String baseUrl,
		@DefaultValue("claude-sonnet-4-6") String model,
		@DefaultValue("1024") int maxTokens,
		@DefaultValue("2023-06-01") String anthropicVersion) {

	public boolean hasApiKey() {
		return apiKey != null && !apiKey.isBlank();
	}
}
