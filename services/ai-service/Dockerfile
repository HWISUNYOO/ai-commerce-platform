# syntax=docker/dockerfile:1
# 1) 빌드 스테이지: Gradle 래퍼로 실행 가능한 jar 생성
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# 2) 실행 스테이지: JRE만 포함해 이미지 경량화
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
