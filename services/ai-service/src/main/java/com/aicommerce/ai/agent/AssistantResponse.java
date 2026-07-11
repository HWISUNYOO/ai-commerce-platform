package com.aicommerce.ai.agent;

import java.util.List;

/**
 * 쇼핑 어시스턴트(에이전트) 응답.
 *
 * @param answer  최종 답변
 * @param steps   에이전트가 거친 도구 호출 흔적(투명성/디버깅용)
 * @param backend 응답을 생성한 LLM 백엔드
 */
public record AssistantResponse(String answer, List<AgentStep> steps, String backend) {
}
