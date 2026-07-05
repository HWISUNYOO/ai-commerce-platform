# infra — 인프라 / 운영

로컬 개발과 클라우드 운영을 위한 인프라 정의.

## 로컬 인프라 (docker-compose)

`docker-compose.yml` — PostgreSQL + Redis + Kafka(KRaft) + Kafka UI 를 한 번에 기동.

```bash
cd infra
docker compose up -d       # 기동
docker compose ps          # 상태
docker compose down        # 중지 (데이터 유지)
docker compose down -v     # 초기화 (볼륨까지 삭제)
```

| 구성요소 | 호스트 접속 | 용도 |
|---|---|---|
| PostgreSQL 17 | `localhost:5432` (postgres/postgres) | 서비스별 DB (member_db/product_db/order_db/payment_db/delivery_db, 최초 기동 시 자동 생성) |
| Redis 7 | `localhost:6379` | 캐시 · 멱등성 |
| Kafka 3.9 (KRaft) | `localhost:9092` | 서비스 간 이벤트 |
| Kafka UI | http://localhost:8090 | Kafka 토픽/메시지 웹 관리 |

> PostgreSQL은 Phase 2의 네이티브 설치본을 대체한다. 컴포즈 기동 시 네이티브
> `postgresql-x64-17` 서비스는 중지되어 있어야 한다(포트 5432 충돌 방지).
> 서비스 접속 정보(localhost:5432, postgres/postgres)는 동일하므로 코드 변경 불필요.

## 향후 (Phase 5)

- `k8s/` — Kubernetes 매니페스트 (AKS 배포)
- `monitoring/` — Prometheus + Grafana
- CI/CD는 저장소 루트 `.github/workflows/` (GitHub Actions)

클라우드 타겟은 Azure (AKS, Azure Database for PostgreSQL) — ADR-0004.
