# infra — 인프라 / 운영

로컬 개발과 클라우드 운영을 위한 인프라 정의.

- `docker-compose.yml` — 로컬 통합 실행 (서비스 + PostgreSQL + Redis + Kafka) — Phase 3
- `k8s/` — Kubernetes 매니페스트 (AKS 배포) — Phase 5
- `monitoring/` — Prometheus + Grafana — Phase 5
- CI/CD는 저장소 루트의 `.github/workflows/`에 둔다 (GitHub Actions) — Phase 5

클라우드 타겟은 Azure (AKS, Azure DB for PostgreSQL) — ADR-0004.
