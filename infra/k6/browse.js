// 상품 조회 부하 테스트 (읽기 중심 — Redis 캐시 효과 관측).
// 실행: k6 run infra/k6/browse.js
//   대상 지정: k6 run -e BASE_URL=http://localhost infra/k6/browse.js
//   (k8s+ingress: http://localhost / 로컬 dev: http://localhost:8082)
import http from 'k6/http'
import { check, sleep } from 'k6'
import { Rate } from 'k6/metrics'

const errors = new Rate('errors')
const BASE = __ENV.BASE_URL || 'http://localhost'

export const options = {
  scenarios: {
    browse: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50 }, // 30초에 걸쳐 50 VU 까지 증가
        { duration: '1m', target: 50 },  // 1분 유지
        { duration: '30s', target: 0 },  // 정리
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'], // p95 지연 500ms 이내
    errors: ['rate<0.01'],            // 에러율 1% 미만
  },
}

export default function () {
  const list = http.get(`${BASE}/api/products`)
  const ok = check(list, { 'list 200': (r) => r.status === 200 })
  if (!ok) errors.add(1)

  const products = list.json()
  if (Array.isArray(products) && products.length > 0) {
    const id = products[Math.floor(Math.random() * products.length)].id
    const detail = http.get(`${BASE}/api/products/${id}`)
    if (!check(detail, { 'detail 200': (r) => r.status === 200 })) errors.add(1)
  }
  sleep(1)
}
