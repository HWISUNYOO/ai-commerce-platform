import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// 개발 서버에서 /api 요청을 각 백엔드 서비스로 프록시한다.
// (CORS 회피 + 단일 오리진처럼 사용. 운영에서는 API Gateway가 이 역할을 대신한다.)
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api/members': 'http://localhost:8081',
      '/api/products': 'http://localhost:8082',
      '/api/orders': 'http://localhost:8083',
      '/api/payments': 'http://localhost:8084',
      '/api/deliveries': 'http://localhost:8085',
      '/api/ai': 'http://localhost:8080',
    },
  },
})
