# 6. MSA 모노레포 구조와 공통 컨벤션

- 상태: 채택(Accepted)
- 날짜: 2026-06-29

## 배경

6개 마이크로서비스 + 게이트웨이 + 프론트엔드(ADR-0003)를 어떻게 저장·빌드·통신·배포할지
일관된 규칙이 필요하다.

## 결정

### 저장소
- **단일 모노레포** (`ai-commerce-platform`). 서비스 경계는 명확하되, 포트폴리오로서
  한 저장소에서 전체를 보여주는 편이 리뷰/탐색에 유리하다.

### 디렉터리 레이아웃
```
ai-commerce-platform/
├── services/   회원·상품·주문·결제·배송·AI (서비스별 독립 Spring Boot)
├── gateway/    API Gateway (Spring Cloud Gateway)
├── frontend/   React
├── infra/      docker-compose, k8s, monitoring
├── docs/adr/   아키텍처 결정 기록
└── .github/workflows/  CI/CD (Phase 5)
```

### 서비스 공통 컨벤션
- 런타임/빌드: **Java 21 + Spring Boot 3.5.x + Gradle**. 서비스마다 **독립 Gradle 빌드**
  (자체 `gradlew`) — 독립 배포 가능성(MSA 원칙) 유지.
- 패키지 네임스페이스: **`com.aicommerce.<service>`** (예: `com.aicommerce.member`).
- 데이터: **Database per Service** — 서비스별 PostgreSQL 스키마. 서비스 간 직접 DB 공유 금지.
- 설정: 12-factor — `application.yml` + 환경변수. 비밀값은 커밋 금지(.gitignore).

### 통신
- **동기:** 클라이언트 → API Gateway → 서비스 (REST). 서비스 간 동기 호출 최소화.
- **비동기:** 도메인 이벤트는 **Kafka**로 발행/구독 (예: 주문 생성 → 결제/배송) — Phase 3.
- **캐시·멱등성:** Redis — Phase 3.

### 서비스 디스커버리
- 로컬(Docker Compose): 컴포즈 서비스명 DNS로 해결 (Eureka 미도입 — 단순성).
- 운영(AKS): Kubernetes 서비스 DNS. 애플리케이션은 호스트명을 환경변수로 주입받아
  벤더 중립 유지(ADR-0004).

## 결과

- 전 서비스가 동일 컨벤션을 공유해 신규 서비스 추가가 기계적이다.
- 모노레포지만 서비스별 독립 빌드라 Docker 이미지/배포는 서비스 단위로 분리된다.
- Eureka 등 별도 디스커버리 서버를 생략해 운영 부담을 줄인다(필요 시 후속 ADR로 재검토).
