package com.aicommerce.ai.llm;

/**
 * LLM 호출 추상화. 구현체(구독 기반 CLI / API 등)를 바꿔 끼울 수 있게 한다(전략 패턴).
 * 상위 로직(RAG 추천 등)은 이 인터페이스에만 의존하므로, 백엔드 교체가 코드 변경 없이 가능하다.
 */
public interface LlmGateway {

	/** 시스템 프롬프트 없이 사용자 프롬프트만으로 응답 텍스트를 받는다. */
	default String complete(String userPrompt) {
		return complete(userPrompt, null);
	}

	/** 시스템 프롬프트(역할/제약)와 사용자 프롬프트로 응답 텍스트를 받는다. */
	String complete(String userPrompt, String systemPrompt);

	/** 활성화된 백엔드 이름(로깅/디버깅용). */
	String backendName();
}
