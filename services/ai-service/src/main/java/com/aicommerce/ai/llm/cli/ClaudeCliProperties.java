package com.aicommerce.ai.llm.cli;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 구독 기반 Claude Code CLI 게이트웨이 설정.
 *
 * @param executable     claude 실행 파일 경로. 비어 있으면 VSCode 확장 설치 위치에서 자동 탐색.
 * @param model          모델 오버라이드(비어 있으면 CLI 기본값 사용).
 * @param timeoutSeconds CLI 프로세스 최대 대기 시간(초).
 */
@ConfigurationProperties(prefix = "llm.cli")
public record ClaudeCliProperties(String executable, String model, int timeoutSeconds) {

	public ClaudeCliProperties {
		if (timeoutSeconds <= 0) {
			timeoutSeconds = 120;
		}
	}
}
