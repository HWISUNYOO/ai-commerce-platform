# 2. SDLC 오케스트레이터를 Spring Boot 3 + Claude API로 구현한다

- 상태: 채택(Accepted)
- 날짜: 2026-06-28

## 배경

목표 파이프라인은 `PM → 분석 → 설계 → 개발 → 테스트 → 배포 → 장애분석`이며,
각 단계는 Claude(LLM)를 호출해 산출물을 만든다. 이 오케스트레이터를 무엇으로
구현할지 선택지가 있었다.

- (A) Claude Code 네이티브: subagent/skill/hook/slash command. 인프라 0, 가장 빠름.
- (B) Claude Agent SDK(TypeScript/Python): 유연한 커스텀 오케스트레이터.
- (C) Spring Boot 3 + Java 21 + Claude API: 직접 구현.

## 결정

**(C) Spring Boot 3.5.16 + Java 21 + Anthropic Claude API**를 채택한다.

근거:

1. 타겟 이직 시장의 핵심 스택이 Java 17/21 + Spring Boot 3.x다. 오케스트레이터
   자체를 이 스택으로 만들면 "AI Agent 설계력"과 "모던 백엔드 역량"을 한 번에 증명한다.
2. 오케스트레이터가 돌기 위해 쓰는 인프라(PostgreSQL/Redis/Kafka/Elasticsearch)가
   곧 데모해야 할 타겟 스택과 일치한다 — 억지로 끼운 기술이 아니라 실제 사용.
3. LLM 호출은 Spring의 `RestClient`로 직접 구현하여 의존성을 최소화한다.

## 개발/테스트 단계의 코드 생성: 하이브리드

오케스트레이터는 "두뇌"(워크플로·상태관리·승인 게이트)를 맡고, 실제 코드 작성이라는
"손"은 단계별로 분리한다.

- 기획/분석/설계/장애분석 → **Claude API 직접 호출**(구조화 산출물, tool use).
- 개발/테스트 → **Claude Code를 headless(`claude -p`)로 호출**해 파일 생성·테스트 실행 위임.

## 비용·인증 주의 (중요)

Claude API(Developer Platform)는 **토큰당 과금**되며 `ANTHROPIC_API_KEY`가 필요하다.
이는 Claude Code Pro/Max 구독과 **별개로 청구**된다. 하이브리드 설계는 무거운
코드 생성 부하를 (구독으로 커버되는) Claude Code로 보내 API 비용을 완화한다.

## 결과

- Java 역량을 결과물 자체로 증명. 단, LLM 오케스트레이션을 Java로 처음부터 구현하는
  부담(스트리밍/tool use/재시도/토큰관리)을 직접 진다 — 학습 가치가 크다.
- API 키 비용 관리가 운영 과제로 추가된다.
