package com.aicommerce.ai.agent;

/**
 * 에이전트가 수행한 한 단계의 흔적(추론 추적용). 어떤 도구를 어떤 인자로 불렀고 무엇을 관찰했는지.
 *
 * @param action      호출한 도구 이름
 * @param args        도구에 넘긴 인자(JSON 문자열)
 * @param observation 도구가 반환한 관찰 결과
 */
public record AgentStep(String action, String args, String observation) {
}
