# services — 마이크로서비스

각 서비스는 독립 배포 가능한 Spring Boot 3.5.x / Java 21 애플리케이션이다. 자체 Gradle 빌드와 DB 스키마를 가진다 (Database per Service).

| 서비스 | 책임 | 데이터스토어 | 상태 |
|---|---|---|---|
| `ai-service` | 상품추천 RAG / 쇼핑 어시스턴트 Agent (LLM 연동) | (Phase 4) | 골격 + LLM PoC |
| `member-service` | 회원 가입/인증/프로필 | PostgreSQL | Phase 2 |
| `product-service` | 상품 카탈로그/재고/검색 | PostgreSQL | Phase 2 |
| `order-service` | 장바구니/주문 | PostgreSQL | Phase 2 |
| `payment-service` | 결제 (도메인 전문성 활용) | PostgreSQL | Phase 2~3 |
| `delivery-service` | 배송 추적/상태 | PostgreSQL | Phase 2~3 |

서비스 간 통신: 동기는 REST(API Gateway 경유), 비동기는 Kafka 이벤트 (Phase 3).
