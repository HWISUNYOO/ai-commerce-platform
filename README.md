# sdlc-agent

> SDLC 전 단계(분석 → 설계 → 개발 → 테스트 → 배포 → 장애분석)를 오케스트레이션하는 AI Agent와,
> 그 Agent가 구축·운영하는 레퍼런스 **결제·정산 MSA**.

이 저장소는 두 개의 결과물을 동등한 완성도로 만든다.

| | 결과물 | 정체 | 증명하는 역량 |
|---|---|---|---|
| **A** | `orchestrator/` | Spring Boot 3 + Java 21 SDLC 오케스트레이터 | AI Agent 설계력 + 모던 Java 백엔드 |
| **B** | `reference-system/` | Agent가 만드는 결제·정산 MSA | Docker/K8s/MSA/Kafka/Redis/PG/검색/Azure |

## 목표 파이프라인

```
[PM/요구사항]
     │
     ▼
Spring Boot 오케스트레이터 (Java 21)  ── Claude API (Messages, tool use)
     │     인프라 = 타겟 스택:  PostgreSQL · Redis · Kafka · Elasticsearch
     ▼
 분석 ▶ 설계 ▶ 개발 ▶ 테스트 ▶ 배포 ▶ 장애분석   (각 단계 = Stage Agent)
     │
     ▼
[레퍼런스 결제·정산 MSA] ─ Docker ─ AKS(Azure) ─ CI/CD ─▶ 운영
```

개발/테스트 단계의 실제 코드 생성은 Claude Code를 headless(`claude -p`)로 호출해 위임한다(하이브리드).
자세한 근거는 [docs/adr/](docs/adr/) 참고.

## 타겟 기술 스택 (이직 시장 빈출)

Java 17/21 · Spring Boot 3.x · Docker · Kubernetes · Redis · Kafka · MSA ·
PostgreSQL · Elasticsearch/OpenSearch · AI(RAG, Agent) · Azure · CI/CD · 테스트 자동화

## 로드맵

- [x] **Phase 0** — 환경 셋업(JDK 21·Git) · 모노레포 골격 · ADR · Spring Boot + Claude API PoC
- [ ] **Phase 1** — 파이프라인 엔진 코어 + 첫 스테이지(Intake → 분석)
- [ ] **Phase 2** — 전체 SDLC 스테이지 + PostgreSQL 산출물 영속 + 승인 게이트
- [ ] **Phase 3** — 개발/테스트 스테이지(Claude Code headless 연동) + 테스트 자동화
- [ ] **Phase 4** — 인프라 통합(Redis/Kafka/ES) + Docker Compose 로컬
- [ ] **Phase 5** — 레퍼런스 결제·정산 MSA를 Agent로 생성 → AKS 배포 + CI/CD
- [ ] **Phase 6** — 장애분석 루프(로그·메트릭 → Claude 분석 → 개선 PR 제안)

## 빠른 시작 (orchestrator)

```bash
cd orchestrator

# Claude API 키 설정 (토큰당 과금, Claude Code 구독과 별개)
#   PowerShell:  $env:ANTHROPIC_API_KEY = "sk-ant-..."
#   bash:        export ANTHROPIC_API_KEY=sk-ant-...

./gradlew bootRun

# 다른 터미널에서:
curl -X POST localhost:8080/api/ping ^
     -H "Content-Type: application/json" ^
     -d "{\"prompt\":\"Say hello from the orchestrator in one sentence.\"}"
```

## 요구 환경

- JDK 21 (Microsoft OpenJDK 등)
- Git
- (Phase 4~) Docker Desktop + WSL2
- Anthropic API 키 (`ANTHROPIC_API_KEY`)
