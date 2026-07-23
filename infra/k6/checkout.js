// 주문 생성 부하 테스트 (쓰기 — Saga 이벤트 체인 처리량 관측).
// 실행: k6 run -e BASE_URL=http://localhost infra/k6/checkout.js
//
// 주의: 주문을 실제로 생성하므로 재고를 소모한다. 지속 부하로 재고가 바닥나면
// 이후 주문은 stock.rejected 로 CANCELLED 된다(그래도 Saga/이벤트 경로는 계속 exercise 됨).
// 순수 처리량 측정이 목적이면 대상 상품 재고를 미리 크게 올려두거나 PRODUCT_ID 를 조정한다.
import http from 'k6/http'
import { check, sleep } from 'k6'

const BASE = __ENV.BASE_URL || 'http://localhost'
const PRODUCT_ID = __ENV.PRODUCT_ID || 1

export const options = {
  scenarios: {
    checkout: {
      executor: 'constant-arrival-rate', // 일정 속도로 주문 유입
      rate: 5,                           // 초당 5건
      timeUnit: '1s',
      duration: '1m',
      preAllocatedVUs: 20,
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<800'],
    http_req_failed: ['rate<0.05'],
  },
}

export default function () {
  const body = JSON.stringify({
    memberId: 1,
    items: [{ productId: Number(PRODUCT_ID), productName: 'load-test', unitPrice: 89000, quantity: 1 }],
  })
  const res = http.post(`${BASE}/api/orders`, body, {
    headers: { 'Content-Type': 'application/json' },
  })
  // 주문 접수(생성)는 성공해야 한다. 이후 확정/취소는 Saga가 비동기로 결정.
  check(res, { 'order accepted': (r) => r.status === 201 || r.status === 200 })
  sleep(0.2)
}
