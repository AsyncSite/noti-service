# Noti Service CI/CD 가이드라인

## 1. 개요

본 문서는 Noti Service의 CI/CD 파이프라인 구축 및 운영을 위한 표준 가이드라인입니다. User Service의 CI/CD 패턴을 동일하게 따릅니다.

## 2. CI/CD 아키텍처

```mermaid
graph LR
    A[개발자 Push] --> B[GitHub]
    B --> C[GitHub Actions]
    C --> D[빌드 & 테스트]
    D --> E[Docker 이미지 생성]
    E --> F[레지스트리 푸시]
    F --> G[배포]
    G --> H[헬스체크]
```

## 3. 사전 요구사항

### 3.1 필수 도구
- Git
- Java 21
- Gradle 8.5+
- Docker 24.0+
- GitHub 계정

### 3.2 필수 설정
- GitHub Personal Access Token (PAT) - GitHub Packages 접근용
- SSH 키 (서버 배포용)
- 환경별 설정 파일 준비
- SMTP 서버 정보 (이메일 발송용)
- Discord Webhook URL (Discord 알림용)

### 3.3 인프라 의존성
Noti Service는 다음 core-platform 서비스에 의존합니다:
- **asyncsite-mysql**: 데이터베이스
- **asyncsite-eureka**: 서비스 디스커버리
- **asyncsite-network**: Docker 네트워크

**중요**: 서버 배포 전에 core-platform이 반드시 먼저 실행되어 있어야 합니다.

배포 전 확인사항:
```bash
# core-platform 실행 상태 확인
docker ps | grep -E "(asyncsite-mysql|asyncsite-eureka)"

# Docker 네트워크 확인
docker network ls | grep asyncsite-network
```

core-platform이 실행되지 않은 경우, noti-service 배포는 실패합니다.

## 4. GitHub Actions 워크플로우

### 4.1 CI/CD Pipeline (`.github/workflows/ci-cd.yml`)
- **트리거**: 
  - Push to main/feature/fix/release/hotfix branches
  - Pull requests to main
  - Manual dispatch
- **작업**:
  1. **Test Job**:
     - Java 21 환경 설정
     - MySQL 서비스 시작
     - Gradle 빌드 및 테스트
     - 테스트 리포트 업로드
  2. **Build and Push Job** (main/feature 브랜치만):
     - Docker 이미지 빌드
     - GitHub Container Registry (ghcr.io)에 푸시
  3. **Deploy Job** (main/feature 브랜치만):
     - SSH를 통한 서버 배포
     - Docker Compose로 서비스 시작
     - 헬스체크

### 4.2 PR Check (`.github/workflows/pr-check.yml`)
- **트리거**: Pull request events
- **작업**:
  - Gradle wrapper 검증
  - 프로젝트 빌드
  - 단위 테스트 실행
  - PR에 테스트 결과 요약 추가

### 4.3 Dependency Check (`.github/workflows/dependency-check.yml`)
- **트리거**: 매주 월요일 자정 (스케줄) 또는 수동
- **작업**:
  - 의존성 취약점 스캔
  - 보안 리포트 생성

## 5. 로컬 개발 환경

### 5.1 Docker Compose 실행

```bash
# 전체 스택 실행 (인프라 포함)
./gradlew dockerUp

# Noti Service만 실행 (인프라가 이미 실행 중일 때)
./gradlew dockerUpNotiOnly

# 컨테이너 중지
./gradlew dockerDown
./gradlew dockerDownNotiOnly

# 로그 확인
./gradlew dockerLogs
./gradlew dockerLogsNotiOnly
```

### 5.2 빌드 및 테스트

```bash
# 테스트 실행
./gradlew test

# JAR 빌드
./gradlew clean build

# Docker 이미지 빌드
./gradlew dockerBuild

# CI 파이프라인 로컬 실행
./scripts/ci-build.sh
```

## 6. 배포 프로세스

### 6.1 자동 배포

1. `main` 브랜치 → 운영 서버 자동 배포
2. `feature/**` 브랜치 → 개발 서버 자동 배포 (선택적)

### 6.2 수동 배포

```bash
# Staging 배포
./scripts/deploy.sh staging

# Production 배포
./scripts/deploy.sh production v1.0.0

# 헬스체크
./scripts/health-check.sh production

# 롤백
./scripts/rollback.sh
```

## 7. 환경 변수 관리

### 7.1 필수 환경 변수

```bash
# Application
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8084

# Database
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/notidb
SPRING_DATASOURCE_USERNAME=noti
SPRING_DATASOURCE_PASSWORD=noti1234

# Eureka
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka/

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Discord
DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/...
```

### 7.2 GitHub Secrets

Repository Settings > Secrets에 설정:
- `PAT_TOKEN` - GitHub Personal Access Token
- `SSH_PRIVATE_KEY` - 서버 접속용 SSH 키 (base64 인코딩)
- `SSH_HOST` - 배포 서버 주소
- `SSH_USER` - SSH 사용자명
- `MAIL_HOST` - SMTP 서버 주소
- `MAIL_PORT` - SMTP 포트
- `MAIL_USERNAME` - 이메일 계정
- `MAIL_PASSWORD` - 이메일 비밀번호
- `MAIL_FROM` - 발신자 이메일
- `DISCORD_WEBHOOK_URL` - Discord Webhook URL

## 8. 모니터링 및 헬스체크

### 8.1 Actuator Endpoints

- Health: `http://localhost:8089/actuator/health`
- Info: `http://localhost:8089/actuator/info`
- Metrics: `http://localhost:8089/actuator/metrics`
- Prometheus: `http://localhost:8089/actuator/prometheus`

### 8.2 헬스체크 스크립트

```bash
# 로컬 환경 헬스체크
./scripts/health-check.sh local

# Production 환경 헬스체크
./scripts/health-check.sh production
```

## 9. 문제 해결

### 9.1 빌드 실패

```bash
# Gradle 캐시 정리
./gradlew clean
rm -rf ~/.gradle/caches

# 의존성 새로고침
./gradlew --refresh-dependencies
```

### 9.2 Docker 문제

```bash
# Docker 캐시 정리
docker system prune -a

# 네트워크 재생성
docker network create asyncsite-network

# 볼륨 재생성
docker volume create asyncsite-mysql-data
```

### 9.3 데이터베이스 연결 실패
### 9.4 이메일 발송 실패 (Illegal address)

증상:
- 로그에 `jakarta.mail.internet.AddressException: Illegal address` 가 출력되고 상태가 FAILED로 기록됨

주요 원인:
- `docker-compose*`에서 `SPRING_MAIL_USERNAME`/`SPRING_MAIL_PASSWORD`가 빈 값으로 주입되어 앱의 기본값을 덮어쓰는 경우
- 코드에서 발신자 주소를 `spring.mail.username` 단일 소스에 의존

해결:
- 구성: `SPRING_MAIL_USERNAME`/`SPRING_MAIL_PASSWORD`에 유효한 기본값을 지정하거나, 해당 env를 제거하여 `application-docker.yml` 기본값이 적용되게 함
- 코드: 발신자 주소는 `application.notification.email.from-address` → 없으면 `spring.mail.username` 순으로 폴백하도록 구현됨(2025-08-16)

점검 체크리스트:
```bash
docker compose config | rg -n "SPRING_MAIL_|APPLICATION_NOTIFICATION_EMAIL_FROM_ADDRESS"
curl -s localhost:8089/actuator/env | rg -n "spring.mail|application.notification.email.from-address"
```


1. MySQL 컨테이너 상태 확인: `docker ps | grep mysql`
2. 데이터베이스 존재 확인: `docker exec -it asyncsite-mysql mysql -p`
3. 네트워크 연결 확인: `docker network inspect asyncsite-network`

## 10. 보안 고려사항

1. **시크릿 관리**: 절대 환경 변수나 비밀번호를 코드에 하드코딩하지 않음
2. **이미지 스캔**: 모든 Docker 이미지는 Trivy로 보안 스캔
3. **의존성 검사**: 정기적인 의존성 취약점 검사
4. **액세스 제어**: Production 배포는 승인된 사용자만 가능

## 11. 성능 최적화

### 11.1 빌드 최적화
- Gradle 빌드 캐시 활용
- Docker 레이어 캐시 최적화
- 병렬 테스트 실행

### 11.2 런타임 최적화
- JVM 힙 크기 조정: `-Xmx512m -Xms256m`
- 데이터베이스 커넥션 풀 최적화
- 비동기 처리 스레드 풀 조정

## 12. 체크리스트

### 12.1 배포 전 체크리스트
- [ ] 모든 테스트 통과
- [ ] 코드 리뷰 완료
- [ ] 환경 변수 설정 확인
- [ ] 데이터베이스 마이그레이션 준비
- [ ] 롤백 계획 수립

### 12.2 배포 후 체크리스트
- [ ] 헬스체크 통과
- [ ] 주요 기능 스모크 테스트
- [ ] 로그 모니터링
- [ ] 성능 메트릭 확인
- [ ] 알림 발송 테스트