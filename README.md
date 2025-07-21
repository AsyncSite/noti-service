# Noti Service

Core Platform의 알림 서비스로, 이메일, Discord, 푸시 알림을 지원하는 마이크로서비스입니다.

## 🏗️ 아키텍처

헥사고날 아키텍처(Hexagonal Architecture)를 기반으로 설계되었습니다:

```
┌─────────────────────────────────────────────────────────────┐
│                    Adapter Layer                            │
├─────────────────────────────────────────────────────────────┤
│  Web Controller  │  Persistence  │  External Services      │
│  (REST API)      │  Adapter      │  (Email, Discord)       │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Application Layer                          │
├─────────────────────────────────────────────────────────────┤
│              Use Case Implementations                       │
│              (NotificationService)                          │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                             │
├─────────────────────────────────────────────────────────────┤
│  Models  │  Ports (In/Out)  │  Domain Services             │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 시작하기

### 사전 요구사항

- Java 21
- Gradle 8.5+
- MySQL 8.0+
- Docker & Docker Compose (선택사항)

### 로컬 개발 환경

1. **데이터베이스 설정**
   ```bash
   # MySQL 실행 (Docker 사용)
   docker run -d --name noti-mysql \
     -e MYSQL_ROOT_PASSWORD=rootpassword \
     -e MYSQL_DATABASE=notidb \
     -p 3308:3306 \
     mysql:8.0
   ```

2. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

3. **Docker로 실행**
   ```bash
   # JAR 빌드
   ./gradlew clean build
   
   # Docker 컨테이너 실행
   docker-compose up -d
   ```

## 📡 API 엔드포인트

### 알림 발송
```http
POST /api/v1/notifications
Content-Type: application/json

{
  "userId": 123,
  "eventType": "STUDY_APPROVED",
  "metadata": {
    "studyTitle": "Java Spring Boot Study",
    "approvedBy": "Admin",
    "studyUrl": "https://example.com/study/123"
  }
}
```

### 알림 조회
```http
GET /api/v1/notifications/{notificationId}
```

### 사용자 알림 목록
```http
GET /api/v1/notifications?userId=123&page=0&size=20
```

### 알림 재시도
```http
POST /api/v1/notifications/{notificationId}/retry
```

### 헬스체크
```http
GET /api/v1/notifications/health
```

## ⚙️ 설정

### 환경변수

```yaml
# 데이터베이스
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/notidb
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=rootpassword

# 이메일 설정
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Discord 설정
DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/...

# Eureka 설정
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
```

### 프로파일

- `local`: 로컬 개발 환경
- `docker`: Docker 환경
- `microservices`: 마이크로서비스 환경

## 🗄️ 데이터베이스 스키마

### 주요 테이블

- `notifications`: 알림 정보
- `notification_channels`: 알림 채널별 발송 정보
- `notification_templates`: 알림 템플릿
- `notification_settings`: 사용자별 알림 설정
- `notification_history`: 알림 히스토리

## 🔧 개발 가이드

### 새로운 알림 타입 추가

1. **템플릿 추가**
   ```sql
   INSERT INTO notification_templates (event_type, channel_type, language, title_template, content_template)
   VALUES ('NEW_EVENT_TYPE', 'EMAIL', 'ko', '제목 템플릿', '내용 템플릿');
   ```

2. **메타데이터 정의**
   ```java
   Map<String, Object> metadata = Map.of(
       "key1", "value1",
       "key2", "value2"
   );
   ```

### 새로운 채널 추가

1. **NotificationChannel.ChannelType에 추가**
2. **NotificationSenderPort 구현체 생성**
3. **설정 파일에 채널별 설정 추가**

## 🧪 테스트

```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest

# 전체 테스트
./gradlew check
```

## 📊 모니터링

### Actuator 엔드포인트

- `/actuator/health`: 헬스체크
- `/actuator/metrics`: 메트릭
- `/actuator/prometheus`: Prometheus 메트릭

### 로그

- 로그 파일: `logs/noti-service.log`
- 로그 레벨: `com.asyncsite=DEBUG`

## 🐳 Docker

### 이미지 빌드
```bash
docker build -t noti-service:latest .
```

### 컨테이너 실행
```bash
docker run -d \
  --name noti-service \
  -p 8084:8084 \
  -e SPRING_PROFILES_ACTIVE=docker \
  noti-service:latest
```

## 📝 TODO

- [ ] NotificationTemplateEntity 및 Repository 구현
- [ ] NotificationSettingsEntity 및 Repository 구현
- [ ] JSON 파싱 유틸리티 구현
- [ ] 템플릿 엔진 개선
- [ ] 재시도 메커니즘 개선
- [ ] 메트릭 수집 강화
- [ ] 보안 설정 추가
- [ ] API 문서화 (Swagger)
- [ ] 통합 테스트 추가

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 