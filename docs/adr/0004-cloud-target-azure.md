# 4. 배포 타겟 클라우드는 Azure로 한다

- 상태: 채택(Accepted)
- 날짜: 2026-06-28

## 배경

커머스 MSA를 컨테이너로 패키징해 Kubernetes에 올리고 CI/CD로
배포한다. 클라우드 벤더로 AWS와 Azure를 검토했다.

## 결정

**Azure**를 1차 배포 타겟으로 한다.

- 컨테이너 오케스트레이션: **AKS**(Azure Kubernetes Service)
- 관계형 DB: **Azure Database for PostgreSQL**
- 검색: Elasticsearch/OpenSearch(자체 호스팅 또는 매니지드 검토)
- 메시징: Kafka(자체 호스팅 또는 Azure Event Hubs Kafka 호환 엔드포인트 검토)
- CI/CD: GitHub Actions → AKS 배포

근거: 국내 대기업·금융권 일부의 Azure 선호, 그리고 AWS 일변도에서 벗어난 기술 다양성 확보.

## 이식성 원칙

특정 벤더 종속을 최소화한다. 애플리케이션은 표준 Kubernetes 매니페스트와
12-factor 설정으로 작성하여, 추후 AWS(EKS) 등으로 이전 가능하도록 한다.

## 결과

- Azure 생태계(AKS/Azure DB/Event Hubs) 학습이 로드맵에 포함된다.
- 로컬 개발은 Docker Compose로, 운영은 AKS로 — 환경 간 패리티를 유지한다.
