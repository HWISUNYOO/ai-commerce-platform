# 트러블슈팅 기록

개발 과정에서 실제로 마주친 문제와 원인·해결을 정리한다. 각 항목은 `증상 → 원인 → 해결 → 교훈` 순서.

---

## 보안 · 인프라

### 1. 로컬 PostgreSQL이 외부에 노출되어 데이터가 삭제됨

- **증상**: docker-compose로 띄운 postgres의 DB 5개가 통째로 삭제되고, `readme_to_recover` DB에 몸값(BTC)을 요구하는 노트가 남음.
- **원인**: compose가 5432 포트를 `0.0.0.0`(모든 인터페이스)에 노출한 상태 + 기본 자격증명(`postgres/postgres`). 인터넷 스캐너 봇의 표적이 됨.
- **해결**: 모든 로컬 인프라 포트를 `127.0.0.1:PORT:PORT` 형태로 **루프백에만 바인딩**(ADR-0008). 볼륨을 재생성하고 서비스는 Flyway로 스키마 재구축.
- **교훈**: 로컬 개발이라도 DB 포트를 `0.0.0.0`에 열지 않는다. 기본 비밀번호 + 전체 노출은 곧바로 공격 대상이 된다.

### 2. Kafka가 `advertised.listeners cannot use 0.0.0.0` 로 기동 실패

- **증상**: apache/kafka 컨테이너가 부팅 중 죽음.
- **원인**: 리스너를 `PLAINTEXT://0.0.0.0:29092`로 설정. advertised listener에는 클라이언트가 실제로 접속할 주소가 필요해 `0.0.0.0`이 금지됨.
- **해결**: 바인딩은 빈 호스트(`PLAINTEXT://:29092`)로, advertised는 내부/호스트를 이중으로 명시(`kafka:29092` 내부, `localhost:9092` 호스트).
- **교훈**: Kafka는 "바인딩 주소"와 "광고 주소"를 구분한다. 광고 주소는 클라이언트가 실제 도달 가능한 이름이어야 한다.

---

## Kafka · 이벤트

### 3. 한 서비스가 여러 토픽을 서로 다른 타입으로 구독할 때 역직렬화 실패

- **증상**: notification-service가 `order.created`/`payment.approved`/`point.earned`를 모두 구독해야 하는데, 단일 `spring.json.value.default.type`으로는 한 타입만 지정 가능.
- **원인**: JsonDeserializer의 기본 타입은 컨슈머 팩토리당 하나. 프로듀서가 타입 헤더를 싣지 않음(`add.type.headers=false`).
- **해결**: 토픽(이벤트)별로 타입을 고정한 `ConcurrentKafkaListenerContainerFactory`를 여러 개 만들고(`@KafkaListener(containerFactory=...)`), `JsonDeserializer.setUseTypeHeaders(false)` + 신뢰 패키지 지정.
- **교훈**: 다중 이벤트 타입 구독은 컨테이너 팩토리를 타입별로 분리한다. 타입 헤더에 의존하지 않으면 서비스 간 패키지명이 달라도 안전하다.

### 4. Saga 전환 후 서비스가 부팅하자마자 NPE / 재고 이중 차감

- **증상**: 재고차감을 이벤트 기반 Saga로 전환하고 order/product 서비스를 재시작하니, 과거 주문 이벤트를 다시 처리하며 NPE가 나고 재고가 잘못 차감됨.
- **원인**: order/product는 이번에 **처음 컨슈머가 된** 서비스라 컨슈머 그룹이 새로 생성됨. `auto-offset-reset=earliest`라 토픽의 **과거 이벤트를 처음부터 재생**. 과거 `order.created`는 구 스키마(항목 목록 `items` 없음)라 `items=null` → 순회 중 NPE. 게다가 이미 차감됐던 과거 주문을 다시 예약 처리.
- **해결**: 신규 컨슈머 그룹인 order/product만 `auto-offset-reset=latest`로 두어 재시작 이후의 신규 이벤트만 처리. (기존 그룹인 point/notification은 커밋된 오프셋부터 재개하므로 `earliest` 유지해도 무방.)
- **교훈**: 기존 토픽에 새 컨슈머 그룹을 붙일 때 `earliest`는 전체 히스토리를 재생한다. 스키마가 진화했거나 과거 이벤트를 재처리하면 안 되는 경우 `latest`를 쓰거나, 이벤트에 버전을 부여한다.

### 5. 브로커 없이 테스트를 그린으로 유지

- **증상**: 서비스에 spring-kafka가 붙으면 통합 테스트(@SpringBootTest)가 브로커에 붙으려다 실패/지연.
- **원인**: 리스너 컨테이너가 기동하며 브로커 연결을 시도.
- **해결**: 테스트 프로파일에서 `spring.kafka.listener.auto-startup=false`(리스너 미기동), 프로듀서는 `app.events.enabled=false` 플래그로 발행 스킵. 커스텀 컨테이너 팩토리도 이 값을 읽어 `setAutoStartup(false)`.
- **교훈**: 이벤트 발행/구독을 플래그로 끌 수 있게 설계하면 브로커 없이도 컨텍스트 로드·API 테스트가 가능하다.

---

## 데이터 정합성

### 6. Kafka at-least-once로 인한 중복 처리

- **증상**: 같은 이벤트가 두 번 전달되면 결제/적립이 중복 생성될 수 있음.
- **원인**: Kafka는 at-least-once 전달을 보장(정확히 한 번이 아님).
- **해결**: **멱등성 이중 방어** — ① Redis `SETNX`(주문 단위 멱등키)로 빠르게 중복을 거르고, ② 금전에 직접 닿는 포인트 적립은 `point_transactions.order_id UNIQUE` 제약을 최종 방어선으로 둠.
- **교훈**: 이벤트 소비자는 항상 중복 전달을 가정한다. 빠른 필터(Redis) + 강한 보장(DB 제약)을 함께 쓰면 견고하다.

### 7. 서비스 간 재고 차감의 동시성

- **증상**: 여러 주문이 동시에 같은 상품을 차감하면 재고가 음수가 될 위험.
- **원인**: "조회 후 차감"을 애플리케이션에서 나눠 하면 경쟁 상태 발생.
- **해결**: `UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty` **단일 SQL**. 재고가 부족하면 0행이 갱신되어 실패를 즉시 감지. 동시 요청에도 DB가 원자적으로 직렬화.
- **교훈**: 재고처럼 경쟁이 심한 갱신은 조건부 단일 UPDATE로 원자화한다.

### 8. Redis 캐시 직렬화에서 `Instant` 실패

- **증상**: 상품을 `@Cacheable`로 캐싱할 때 `Instant` 타임스탬프 직렬화/역직렬화 오류.
- **원인**: `GenericJackson2JsonRedisSerializer`가 자바 8 시간 타입을 기본 처리하지 못함.
- **해결**: `JavaTimeModule`을 등록한 `ObjectMapper`로 만든 타입 지정 `Jackson2JsonRedisSerializer`를 사용.
- **교훈**: Redis JSON 직렬화 시 자바 8 날짜/시간은 JavaTimeModule을 명시 등록해야 한다.

---

## 빌드 · 실행 환경

### 9. 컨테이너 빌드에서 `gradlew` 실행 실패 (CRLF)

- **증상**: 리눅스 빌드 컨테이너에서 `./gradlew`가 `bad interpreter` 등으로 실패.
- **원인**: 윈도우에서 작성된 `gradlew`가 CRLF 줄바꿈을 가짐.
- **해결**: Dockerfile 빌드 단계에서 `sed -i 's/\r$//' gradlew && chmod +x gradlew`.
- **교훈**: 크로스플랫폼 스크립트는 컨테이너에서 줄바꿈을 정규화한다.

### 10. WSL2 활성화 후 개발 서버 포트가 "사용 중"

- **증상**: Vite dev 서버가 5173 포트를 못 잡고 5181로 밀림. 그런데 `netstat`상 5173에는 아무도 리스닝하지 않음.
- **원인**: WSL2/Hyper-V가 활성화되면 Windows가 동적 포트 범위 일부(여기선 5173~5180 포함)를 **예약**해, 실제 리스너가 없어도 앱이 바인딩에 실패(EADDRINUSE).
- **해결**: 밀려난 포트(5181)를 그대로 사용하거나, 필요 시 예약 범위 밖 포트로 고정.
- **교훈**: WSL2/Docker Desktop 환경에서 특정 포트가 이유 없이 막히면 Windows의 예약 포트 범위(`netsh interface ipv4 show excludedportrange tcp`)를 의심한다.

### 11. 로컬 k8s에서 이미지 Pull 실패 여부

- **증상**: Docker Desktop 쿠버네티스(kind 기반, 노드 `desktop-control-plane`)에 배포 시 로컬 빌드 이미지를 못 찾을까 우려.
- **원인**: kind류 클러스터는 별도 이미지 저장소를 쓰기도 해 로컬 `docker build` 이미지가 클러스터에 자동 공유되지 않는 경우가 있음.
- **해결**: 확인 결과 Docker Desktop k8s는 로컬 이미지가 공유됨 → `imagePullPolicy: IfNotPresent`로 레지스트리 없이 배포 가능. 공유되지 않는 환경이라면 이미지를 클러스터에 로드하거나 레지스트리를 사용.
- **교훈**: 로컬 k8s에 배포하기 전, 로컬 이미지가 클러스터에 보이는지부터 확인한다.

### 12. 패키지 매니저(winget) 소스 손상

- **증상**: `winget install`이 `0x8a15000f`(소스 데이터 누락)로 실패, 소스 리셋도 복구 안 됨.
- **원인**: winget 소스 인덱스 손상/네트워크 간헐 오류.
- **해결**: 설치 파일을 `curl`로 공식 배포처에서 직접 내려받아 무인 설치(`msiexec /quiet` 등).
- **교훈**: 패키지 매니저가 막히면 공식 배포처 직접 다운로드가 확실한 우회로다.
