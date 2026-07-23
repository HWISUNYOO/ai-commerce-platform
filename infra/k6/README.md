# 성능 테스트 (k6)

[k6](https://k6.io/)로 API에 부하를 걸어 처리량·지연(p95/p99)·에러율을 측정한다.
부하를 거는 동안 Grafana(`localhost:3000`)에서 서비스별 JVM/HTTP 지표 변화를 함께 관측한다.

## 준비

1. 스택 기동 (둘 중 하나)
   - 쿠버네티스: `kubectl apply -f infra/k8s/` → 대상 `http://localhost`
   - 로컬 dev: `docker compose -f infra/docker-compose.yml up -d` + 각 서비스 `bootRun` → 대상 `http://localhost:8082` 등
2. k6 설치 (미설치 시): `winget install k6` 또는 https://k6.io/docs/get-started/installation

## 실행

```powershell
# 읽기 부하 (상품 조회, 캐시 효과)
k6 run -e BASE_URL=http://localhost infra/k6/browse.js

# 쓰기 부하 (주문 → Saga 체인)
k6 run -e BASE_URL=http://localhost infra/k6/checkout.js
```

## 시나리오

| 스크립트 | 시나리오 | 관찰 포인트 |
|----------|----------|-------------|
| `browse.js` | 50 VU 까지 램프업하며 상품 목록/상세 조회 | Redis 캐시 히트율, p95 지연, 조회 처리량 |
| `checkout.js` | 초당 5건 주문 유입(1분) | 주문 접수 지연, Saga 이벤트 처리량, Kafka consumer lag |

## 임계값(threshold)

각 스크립트에 SLO 성격의 임계값이 설정되어 있다(예: p95 < 500ms, 에러율 < 1%).
초과 시 k6가 실패로 표시하므로 CI 게이트로도 활용 가능하다.
