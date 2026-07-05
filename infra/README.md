# infra — 인프라 / 운영

로컬 개발과 클라우드 운영을 위한 인프라 정의.

## 로컬 실행 (docker-compose)

- `docker-compose.yml` — 인프라: PostgreSQL · Redis · Kafka(KRaft) · Kafka UI
- `docker-compose.apps.yml` — 애플리케이션: member/product/order/payment/delivery 서비스

```bash
cd infra

# 인프라만 (서비스는 gradlew bootRun 으로 로컬 실행할 때)
docker compose up -d

# 전체 스택 (인프라 + 5개 서비스 모두 컨테이너로)
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build

# 상태 / 중지 / 초기화
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps
docker compose -f docker-compose.yml -f docker-compose.apps.yml down       # 데이터 유지
docker compose -f docker-compose.yml -f docker-compose.apps.yml down -v    # 볼륨까지 삭제
```

| 구성요소 | 접속 | 용도 |
|---|---|---|
| PostgreSQL 17 | `127.0.0.1:5432` (postgres/postgres) | 서비스별 DB (최초 기동 시 자동 생성) |
| Redis 7 | `127.0.0.1:6379` | 캐시 · 멱등성 |
| Kafka 3.9 (KRaft) | `127.0.0.1:9092` | 서비스 간 이벤트 |
| Kafka UI | http://localhost:8090 | Kafka 토픽/메시지 웹 관리 |
| 서비스 | `127.0.0.1:8081~8085` | member/product/order/payment/delivery |

### ⚠️ 보안: 포트는 반드시 `127.0.0.1` 로만 바인딩

모든 포트를 `127.0.0.1:PORT:PORT` 로 묶어 **이 PC에서만** 접근 가능하게 한다.
`"5432:5432"` 처럼 쓰면 `0.0.0.0`(모든 인터페이스)에 노출되어, 기본 비밀번호를 노리는
자동 스캔 봇의 표적이 된다(특히 PostgreSQL·Redis). 실제로 개발 중 노출된 Postgres가
랜섬웨어 봇에 DB를 삭제당한 사례가 있어 전 포트를 로컬로 제한했다 — ADR-0008 참고.

> 컨테이너 간 통신은 도커 네트워크에서 서비스명(`postgres`/`redis`/`kafka`)으로 하므로,
> 호스트 포트 노출을 `127.0.0.1` 로 제한해도 서비스 동작에는 영향이 없다.

## 향후 (Phase 5)

- `k8s/` — Kubernetes 매니페스트 (AKS 배포)
- `monitoring/` — Prometheus + Grafana
- CI/CD는 저장소 루트 `.github/workflows/` (GitHub Actions)

클라우드 타겟은 Azure (AKS, Azure Database for PostgreSQL) — ADR-0004.
