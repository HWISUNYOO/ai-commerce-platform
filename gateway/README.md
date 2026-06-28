# gateway — API Gateway

외부 요청의 단일 진입점. **Spring Cloud Gateway** 기반으로 라우팅, 인증 토큰 검증, 공통 필터(로깅/레이트리밋)를 담당한다.

- 클라이언트(React frontend) → gateway → 각 마이크로서비스
- 서비스 디스커버리: 로컬(Docker Compose)에서는 컴포즈 서비스명 DNS, 운영(AKS)에서는 Kubernetes 서비스 DNS
- Phase 1에서 골격, Phase 2~3에서 라우트/인증 채움
