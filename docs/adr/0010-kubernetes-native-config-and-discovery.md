# 10. 서비스 디스커버리·설정은 쿠버네티스 네이티브로 한다 (Eureka·Config Server 미도입)

- 상태: 채택(Accepted)
- 날짜: 2026-07-19

## 배경

MSA의 서비스 디스커버리·중앙 설정 관리로 흔히 Spring Cloud Netflix 계열
(Eureka + Spring Cloud Config Server)을 떠올린다. 이 프로젝트도 초기에는 이들 도입을
검토했다. 그러나 최종 배포 타깃이 **쿠버네티스**이고, 쿠버네티스는 같은 문제를 플랫폼
차원에서 이미 해결한다.

## 결정

**Eureka와 Spring Cloud Config Server를 도입하지 않는다.** 대신 쿠버네티스 네이티브 기능을 쓴다.

- **서비스 디스커버리**: 쿠버네티스 `Service` + 클러스터 DNS. 서비스는 `http://product-service:8082`
  처럼 **서비스명**으로 서로를 찾는다(Pod IP가 바뀌어도 이름은 고정).
- **설정/비밀**: `ConfigMap`(공통 설정: Kafka 주소, Redis 호스트) + `Secret`(DB 자격 등).
  각 Deployment가 `envFrom`/`valueFrom`으로 주입받는다.

## 근거

1. **역할 중복 제거** — Eureka(디스커버리)·Config Server(설정)의 역할을 쿠버네티스의
   Service/DNS·ConfigMap/Secret이 그대로 수행한다. 둘을 함께 두면 같은 일을 하는 계층이
   중복되어 "왜 둘 다 있나"라는 운영·이해 부담만 커진다.
2. **인프라 결합 최소화** — 디스커버리/설정을 플랫폼에 위임하면 애플리케이션 코드와
   별도 인프라 서비스(등록/헬스체크/설정 서버) 운영 부담이 준다.
3. 로컬 개발에서는 `application.yml`의 환경변수 기본값으로, 쿠버네티스에서는
   ConfigMap/Secret 오버라이드로 동일 코드가 동작한다(12-factor 설정 외부화).

## 결과 / 후속

- 진입점(외부 → 내부 라우팅)은 **Ingress**(ingress-nginx)가 담당한다. 개발에서는 Vite
  프록시가 같은 역할을 한다.
- Spring Cloud Gateway는 현재 사용하지 않는다. 인증·rate limit·요청 변환 등
  **애플리케이션 레벨 게이트웨이 정책**이 필요해지면 Ingress 뒤에 도입을 재검토한다.
- 트레이드오프: Spring Cloud 스택 경험을 코드로 보여주지는 않는다. 그러나 쿠버네티스로
  가는 아키텍처에서는 네이티브 방식이 더 정합적이라고 판단했다.
