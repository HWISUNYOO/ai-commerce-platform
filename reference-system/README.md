# reference-system — 결제·정산 MSA

이 디렉터리는 오케스트레이터(Agent)가 설계·구현·배포·운영하는 **레퍼런스 결제·정산 시스템**이
들어갈 자리다. Phase 5에서 Agent가 본격적으로 채운다.

## 잠정 서비스 구성 (ADR-0003 초안)

| 서비스 | 책임 |
|---|---|
| `payment-service` | 결제 승인/취소, 멱등성 보장 |
| `ledger-service` | 원장/전표 기록 |
| `settlement-service` | 정산/대사(reconciliation) 배치 |
| `notification-service` | 이벤트 기반 알림 |

공통 인프라: Kafka(이벤트) · Redis(멱등키/캐시) · PostgreSQL(원장) · Elasticsearch/OpenSearch(거래 검색)
배포: Docker → AKS(Azure) → GitHub Actions CI/CD

> 현재는 자리표시자(placeholder)다. 구체 설계는 Agent의 분석/설계 단계 산출물로 생성된다.
