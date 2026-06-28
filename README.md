# ai-commerce-platform

> 일반 상품 커머스를 **MSA**로 구축하고 **AI(RAG/Agent)** 기능을 더한 커머스 플랫폼.
> 회원·상품·주문·결제·배송·AI 6개 서비스 + API Gateway + React 프론트엔드.

## 아키텍처

```
            [React frontend]
                  │
                  ▼
        [API Gateway · Spring Cloud Gateway]
   ┌──────┬──────┬──────┬──────┬──────┬──────┐
   ▼      ▼      ▼      ▼      ▼      ▼
 member product order payment delivery  ai      ← Spring Boot 3.5 / Java 21
   │      │      │      │      │
   └──────┴──────┴──────┴──────┘   각자 PostgreSQL (Database per Service)
        ↕ Kafka(이벤트) · Redis(캐시/멱등성)
                  │
          Docker → AKS(Azure) ─ Prometheus/Grafana ─ GitHub Actions
```

설계 근거는 [docs/adr/](docs/adr/) 참고.

## 기술 스택

Java 21 · Spring Boot 3.5 · Spring Cloud Gateway · PostgreSQL · Redis · Kafka ·
Docker · Kubernetes(AKS) · Prometheus/Grafana · GitHub Actions · React · AI(RAG/Agent)

## 디렉터리

| 경로 | 내용 |
|---|---|
| `services/` | 6개 마이크로서비스 (member/product/order/payment/delivery/ai) |
| `gateway/` | API Gateway |
| `frontend/` | React 쇼핑 UI |
| `infra/` | docker-compose · k8s · monitoring |
| `docs/adr/` | 아키텍처 결정 기록 |

## 로드맵 (6 Phase)

- [ ] **Phase 1** — 개발환경 · GitHub 저장소 · 기본 아키텍처 · MSA 골격 ← **진행 중**
- [ ] **Phase 2** — 회원·상품·주문 서비스 + PostgreSQL
- [ ] **Phase 3** — Redis · Kafka · Docker
- [ ] **Phase 4** — AI 서비스(RAG/Agent) + Azure AI-103 학습 병행
- [ ] **Phase 5** — Kubernetes · 모니터링(Prometheus/Grafana) · GitHub Actions
- [ ] **Phase 6** — 성능 테스트 · 트러블슈팅 문서화 · 문서 정리

## 현재 동작하는 것

`ai-service`에 LLM 연동 PoC가 들어 있다 (ADR-0005: 기본 백엔드 = 구독 기반 Claude Code CLI).

```powershell
cd services/ai-service
./gradlew test          # 컨텍스트 로드 검증
```

## 요구 환경

- JDK 21 · Git
- (Phase 3~) Docker Desktop + WSL2
- (옵션) `ANTHROPIC_API_KEY` — ai-service의 API 백엔드 사용 시
