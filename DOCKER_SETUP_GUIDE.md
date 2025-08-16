
# Noti Service - Docker 구성 가이드

## 1. 개요

본 문서는 **Noti Service**의 Docker 환경 구성 및 운영 가이드를 제공합니다. Core Platform과 통합되어 작동하는 마이크로서비스로서의 컨테이너화 전략과 베스트 프랙티스를 포함합니다.

---

## 2. 프로젝트 구조

```
noti-service/
├── Dockerfile                # 프로덕션용 Dockerfile
├── Dockerfile.multistage     # 멀티스테이지 빌드 Dockerfile
├── docker-compose.yml        # 독립 실행용 구성
├── build-docker.sh           # Docker 빌드 스크립트
├── docker-tasks.gradle.kts   # Gradle Docker 태스크
├── mysql/
│   ├── conf/
│   │   └── custom.cnf        # MySQL 커스텀 설정
│   └── init/
│       └── 01-create-noti-databases.sql  # DB 초기화
└── .dockerignore             # Docker 빌드 제외 파일
```

---

## 3. 사전 요구사항

### 3.1. 필수 소프트웨어

- Docker Engine 24.0 이상

- Docker Compose 2.20 이상

- Java 21 (빌드용)

- Gradle 8.5 이상


### 3.2. Core Platform 통합

Noti Service는 Core Platform의 인프라를 활용합니다:

``` bash
# Core Platform 네트워크 연결 확인
docker network ls | grep asyncsite-network

# 공유 볼륨 확인 (필요시)
docker volume ls | grep asyncsite
```

---

## 4. Dockerfile 구성

### 4.1. 프로덕션 Dockerfile (권장)

GitHub 패키지 인증 문제를 회피하기 위한 사전 빌드 방식입니다.


``` Dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

# 보안을 위한 non-root 사용자 생성
RUN groupadd -g 1001 appgroup && \
    useradd -r -u 1001 -g appgroup appuser

# 사전 빌드된 JAR 파일 복사
COPY --chown=appuser:appgroup build/libs/*.jar app.jar

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8089/actuator/health || exit 1

USER appuser
EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4.2. 멀티스테이지 Dockerfile (CI/CD용)



``` Dockerfile
# 빌드 스테이지
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Gradle 래퍼 및 설정 파일 복사
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 의존성 다운로드 (캐시 최적화)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew clean build -x test --no-daemon

# 런타임 스테이지
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd -g 1001 appgroup && \
    useradd -r -u 1001 -g appgroup appuser

COPY --from=builder --chown=appuser:appgroup /workspace/build/libs/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8089/actuator/health || exit 1

USER appuser
EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 5. Docker Compose 구성

### 5.1. 독립 실행 구성 (docker-compose.yml)

`noti-service`를 위한 독립적인 개발 환경 구성입니다.

```YAML
version: '3.8'

services:
  noti-service:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: noti-service
    ports:
       - "8089:8089"
    environment:
      # Spring 프로파일
      - SPRING_PROFILES_ACTIVE=docker,microservices
      # Eureka 설정
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - EUREKA_INSTANCE_PREFER_IP_ADDRESS=true
      # 데이터베이스 설정
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/notidb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=rootpassword
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - asyncsite-network
    volumes:
      - ./logs:/app/logs
    healthcheck:
       test: ["CMD", "curl", "-f", "http://localhost:8089/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
    labels:
      - "com.asyncsite.service=noti"
      - "com.asyncsite.tier=business"

  # 로컬 개발용 인프라 (Core Platform 미사용 시)
  mysql:
    image: mysql:8.0
    container_name: noti-mysql
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=notidb
    volumes:
      - noti-mysql-data:/var/lib/mysql
      - ./mysql/conf:/etc/mysql/conf.d
      - ./mysql/init:/docker-entrypoint-initdb.d
    ports:
      - "3308:3306"  # Core Platform MySQL과 충돌 방지
    networks:
      - asyncsite-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  asyncsite-network:
    external: true

volumes:
  noti-mysql-data:
```

### 5.2. Core Platform 통합 구성

Core Platform의 공유 인프라(MySQL, Redis 등)를 사용하는 경우의 `docker-compose.yml`입니다.

```YAML
version: '3.8'

services:
   noti-service:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: noti-service
    ports:
       - "8089:8089"
    environment:
      - SPRING_PROFILES_ACTIVE=docker,microservices
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_DATASOURCE_URL=jdbc:mysql://asyncsite-mysql:3306/notidb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=rootpassword
    networks:
      - asyncsite-network
    external_links:
      - asyncsite-mysql:mysql
      - eureka-server

networks:
  asyncsite-network:
    external: true
```

---

## 6. MySQL 설정

### 6.1. 커스텀 설정 (`mysql/conf/custom.cnf`)

(User Service와 동일한 MySQL 설정을 사용합니다.)

### 6.2. 데이터베이스 초기화 (`mysql/init/01-create-noti-databases.sql`)



```SQL
-- Noti Service 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS notidb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 테스트 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS noyidb_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 권한 설정
GRANT ALL PRIVILEGES ON notidb.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON notidb_test.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

---

## 7. 빌드 및 실행

### 7.1. 수동 실행



```Bash
# JAR 파일 빌드
./gradlew clean build

# Docker 이미지 빌드
docker build -t noti-service:latest .

# 컨테이너 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f noti-service
```

---

## 8. 환경변수 관리

### 8.1. 필수 환경변수 (`.env` 파일 예시)



```
# Spring 프로파일
SPRING_PROFILES_ACTIVE=docker,microservices

# Eureka
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/

# 데이터베이스
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/notidb
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=rootpassword

# 로깅
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_ASYNCSITE=DEBUG
```

---

## 9. 헬스체크 및 모니터링

### 9.1. 헬스체크 엔드포인트



```Bash
# 기본 헬스체크
curl http://localhost:8084/actuator/health

# 상세 헬스체크
curl http://localhost:8084/actuator/health/liveness
curl http://localhost:8084/actuator/health/readiness
```

### 9.2. 메트릭 모니터링



```Bash
# Prometheus 메트릭
curl http://localhost:8084/actuator/prometheus
```

---

## 10. 로깅

### 10.1. 로그 설정 (`application-docker.yml`)



```YAML
logging:
  file:
    name: /app/logs/noti-service.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  level:
    root: INFO
    com.asyncsite: DEBUG
```

### 10.2. 로그 확인



```Bash
# 실시간 로그
docker logs -f noti-service

# 파일 로그
docker exec noti-service tail -f /app/logs/noti-service.log
```

---

## 11. 보안 고려사항

- **이미지 보안**: Non-root 사용자 실행, 최소 권한 원칙을 준수합니다.

- **네트워크 보안**: 필요한 포트(8084)만 노출하고, 내부 서비스는 Docker 네트워크로만 통신합니다.


---

## 12. 트러블슈팅

### 12.1. 시작 실패



```Bash
# 로그 확인
docker logs noti-service

# 환경변수 확인
docker exec noti-service env

# 네트워크 연결 테스트
docker exec noti-service ping mysql
```

### 12.2. 데이터베이스 연결 실패



```Bash
# MySQL 상태 확인
docker exec noti-mysql mysql -uroot -prootpassword -e "SHOW DATABASES;"

# 연결 테스트
docker exec noti-service apt-get update && apt-get install -y default-mysql-client
docker exec noti-service mysql -h noti-mysql -uroot -prootpassword notidb
```

---

## 13. CI/CD 통합

### GitHub Actions 예시



```YAML
- name: Build and push Docker image
  run: |
    ./gradlew clean build
    docker build -t ${{ secrets.DOCKER_REGISTRY }}/noti-service:${{ github.sha }} .
    docker push ${{ secrets.DOCKER_REGISTRY }}/noti-service:${{ github.sha }}
```

---

## 14. 체크리스트

Noti Service Docker 구성 시 확인사항:

- [ ] Core Platform 네트워크 연결 확인

- [ ] JAR 파일 사전 빌드

- [ ] Dockerfile 작성 (non-root user, 포트 `8084)

- [ ] `docker-compose.yml` 구성 (`noti-service` 기준)

- [ ] MySQL 초기화 스크립트 준비 (`notidb` 생성)

- [ ] 환경변수 설정 (.env 파일)

- [ ] 헬스체크 엔드포인트 설정 (`8084` 포트)

- [ ] 로그 디렉토리 마운트

- [ ] 네트워크 통신 테스트

- [ ] 리소스 제한 설정

- [ ] 보안 설정 확인
