# 예약 알림 (Scheduled Notifications) 구현

## 개요
noti-service에 예약 발송 기능을 추가하여 특정 시간에 알림을 자동으로 발송할 수 있도록 구현했습니다.

## 주요 구현 내용

### 1. 도메인 모델 수정
- `NotificationStatus` enum에 `SCHEDULED` 상태 추가
- `Notification` 도메인 모델에 `scheduledAt` 필드 추가
- `createScheduled()` 정적 팩토리 메서드 추가
- `isScheduled()`, `shouldBeSentNow()` 메서드 추가

### 2. CAS (Compare-And-Swap) 동시성 제어
- `NotificationRepository`에 CAS 업데이트 메서드 추가
- 버전 체크를 통한 동시성 안전 보장
- 중복 처리 방지를 위한 멱등성 보장

### 3. 스케줄러 구현
- `NotificationScheduler` 컴포넌트 추가
- Spring @Scheduled를 사용한 주기적 처리 (1분마다)
- 예약 시간이 된 알림을 자동으로 처리

### 4. API 확장
- `NotificationUseCase`에 예약 알림 생성 메서드 추가
- `createScheduledNotification()` - 단일 수신자
- `createScheduledNotificationBulk()` - 다중 수신자

## 사용 방법

### 예약 알림 생성
```bash
curl -X POST http://localhost:8080/api/noti \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "channelType": "EMAIL",
    "eventType": "NOTI",
    "recipientContact": "user@example.com",
    "scheduledAt": "2025-09-22T10:30:00",
    "metadata": {
      "templateId": "welcome-email",
      "variables": {
        "userName": "사용자명"
      }
    }
  }'
```

### 설정 옵션
```yaml
# application.yml
notification:
  scheduler:
    enabled: true              # 스케줄러 활성화 (기본: true)
    interval: 60000           # 실행 주기 (밀리초, 기본: 60000 = 1분)
    batch-size: 100           # 한 번에 처리할 알림 개수 (기본: 100)
    pending:
      interval: 300000        # PENDING 재처리 주기 (밀리초, 기본: 300000 = 5분)
```

## 아키텍처

### 처리 흐름
1. **예약 생성**: 클라이언트가 `scheduledAt`과 함께 알림 생성 요청
2. **상태 저장**: 알림이 `SCHEDULED` 상태로 DB에 저장
3. **스케줄러 실행**: 매분 스케줄러가 예약 시간이 된 알림 검색
4. **상태 변경**: `SCHEDULED` → `PENDING`으로 원자적 업데이트
5. **큐 발행**: NotificationCommand를 큐에 발행
6. **발송 처리**: CommandHandler가 실제 발송 수행
7. **상태 완료**: `PENDING` → `SENT` 또는 `FAILED`

### 동시성 처리
- **CAS 패턴**: 버전 체크를 통한 낙관적 잠금
- **멱등성**: 이미 처리된 알림은 스킵
- **재시도**: 실패 시 exponential backoff로 재시도

## 테스트
```bash
# 단위 테스트 실행
./gradlew test

# 예약 알림 통합 테스트
./test-scheduled-notification.sh
```

## 모니터링
예약 알림 관련 로그:
- `NotificationScheduler`: 스케줄러 실행 로그
- `NotificationCommandHandler`: CAS 업데이트 성공/실패 로그
- `NotificationService`: 예약 알림 생성 로그

## 주의사항
1. 예약 시간은 반드시 미래 시간이어야 함
2. 스케줄러 주기(기본 1분) 내에서 시간 정확도가 결정됨
3. 서버 재시작 시에도 예약 알림은 유지됨 (DB 영속성)