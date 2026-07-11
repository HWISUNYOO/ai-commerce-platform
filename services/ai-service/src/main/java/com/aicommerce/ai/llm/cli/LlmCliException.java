package com.aicommerce.ai.llm.cli;

/** Claude CLI 실행/파싱 실패를 나타내는 런타임 예외. */
public class LlmCliException extends RuntimeException {

	public LlmCliException(String message) {
		super(message);
	}

	public LlmCliException(String message, Throwable cause) {
		super(message, cause);
	}
}
