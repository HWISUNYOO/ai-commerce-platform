# 9. 주문 처리는 이벤트 기반 Choreography Saga로 한다

- 상태: 채택(Accepted)
- 날짜: 2026-07-19

## 배경

주문 한 건은 여러 서비스에 걸친 트랜잭션이다: 주문 저장(order_db) · 재고 차감(product_db) ·
결제(payment_db) · 포인트 적립(point_db). 서비스마다 DB가 분리되어 있어(Database per Service)
**하나의 로컬 트랜잭션으로 묶을 수 없다.**

초기에는 order-service가 주문 시 product-service의 재고 차감 API를 **동기 REST**로 호출했다.
이 방식은 단순하지만, 재고를 차감한 뒤 결제 단계에서 실패하면 이미 차감한 재고를 되돌릴
경로가 없다. 분산 트랜잭션(2PC)은 서비스 간 결합과 잠금 비용이 커 MSA에 부적합하다.

## 결정

주문 흐름을 **Choreography Saga**(중앙 오케스트레이터 없이 각 서비스가 이벤트에 반응해
다음 이벤트를 발행)로 구현한다. 주문 상태는 `PENDING → CONFIRMED / CANCELLED` 생명주기를 가진다.

정방향:

```
order.created(+items) → product 재고예약 → stock.reserved
    → payment 결제 → payment.approved → order CONFIRMED (+ point 적립 → point.earned → notification)
```

실패 · 보상:

```
재고 부족  → stock.rejected → order CANCELLED
결제 실패  → payment.failed → product 재고 복원(보상) + order CANCELLED + 취소 알림
```

- 결제 트리거는 `order.created`가 아니라 **`stock.reserved`** 다(재고 확보 후에만 결제).
- 각 서비스는 자신이 소비하는 이벤트 계약(record)을 **자기 패키지에 소유**한다.
- product-service는 예약 내역(`stock_reservations`)을 남겨, 보상 시 정확히 되돌린다.

## 근거

1. **느슨한 결합** — 서비스는 서로를 직접 호출하지 않고 이벤트로만 협력한다.
2. **실패 복원** — 결제 실패 등 하위 단계 실패 시, 보상 이벤트로 앞 단계(재고)를 되돌려
   최종적으로 일관된 상태(주문 취소 + 재고 원복)에 도달한다.
3. **잠금 없음 · 확장성** — 2PC의 전역 잠금·조율자 없이 각 서비스의 로컬 트랜잭션만 사용.
4. Orchestration(중앙 조율자) 대비 구현이 단순하고, 이벤트 인프라(Kafka)를 이미 보유.

## 결과 / 후속

- **중간 상태가 노출된다.** 주문이 잠시 `PENDING`으로 존재하고, 재고 부족은 즉시 409가
  아니라 주문 생성 후 비동기로 `CANCELLED`가 된다. 프론트는 주문완료 화면에서 상태를
  폴링해 최종 결과(CONFIRMED/CANCELLED)를 보여준다.
- **멱등성 필수.** Kafka at-least-once 대비 각 소비자는 Redis `SETNX` + (포인트는) DB
  UNIQUE 제약으로 중복 처리를 막는다. 상태 전이(confirm/cancel)는 `PENDING`일 때만 일어나
  중복·역전 이벤트에 안전하다.
- **한계.** Choreography는 흐름이 코드 여기저기에 흩어져 전체 파악이 어려울 수 있다. 흐름이
  더 복잡해지면 Orchestration(Saga 오케스트레이터) 도입을 재검토한다.
- 교훈: 분산 트랜잭션은 "다 함께 커밋"이 아니라 "하나씩 하고, 틀어지면 되돌린다".
