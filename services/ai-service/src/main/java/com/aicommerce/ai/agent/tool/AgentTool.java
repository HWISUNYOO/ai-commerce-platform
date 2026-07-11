package com.aicommerce.ai.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 에이전트가 호출할 수 있는 도구. 이름/설명은 프롬프트에 노출되어 모델이 언제 부를지 판단하고,
 * {@link #execute}는 모델이 준 인자로 실제 작업을 수행해 관찰(observation) 텍스트를 돌려준다.
 */
public interface AgentTool {

	/** 모델이 action으로 지정하는 도구 이름. */
	String name();

	/** 프롬프트에 넣을 사용법 설명(인자 포함). */
	String description();

	/** 모델이 준 args(JSON)로 도구를 실행하고 관찰 결과를 반환한다. */
	String execute(JsonNode args);
}
