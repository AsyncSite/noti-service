# Optimistic Locking Issue in Notification Service

## 문제 설명
Kafka 이벤트를 통해 알림을 처리할 때 `ObjectOptimisticLockingFailureException` 발생

## 에러 로그
```
org.springframework.orm.ObjectOptimisticLockingFailureException: Row was updated or deleted by another transaction 
(or unsaved-value mapping was incorrect): [com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationEntity#3777dc0c-e892-47ef-83a7-bf2caab1183a]
```

## 문제 발생 흐름

### 1. PasskeyOtpEventListener (Kafka 이벤트 수신)
```java
// Kafka에서 이벤트 수신
notificationUseCase.createNotification(...) // 호출
```

### 2. NotificationService.createNotification()
```java
// Line 105: 첫 번째 저장 (상태: PENDING)
notification = notificationRepository.saveNotification(notification);

// Line 107: 내부 이벤트 발생
notiEventSender.notiCreated(notification);
```

### 3. NotiEventHandler (비동기 처리)
```java
@Async
@EventListener
@Transactional(propagation = Propagation.REQUIRES_NEW) // 새로운 트랜잭션
public void on(NotificationCreated event) {
    // Line 26: sendNotification 호출
    notificationUseCase.sendNotification(event.notification());
}
```

### 4. NotificationService.sendNotification()
```java
// Line 136: 실제 이메일 발송
Notification sendNotification = sender.sendNotification(notification);

// Line 137: 두 번째 저장 시도 (문제 발생!)
notificationRepository.saveNotification(sendNotification);
```

## 문제 원인

1. **동일 엔티티 중복 저장**: 같은 Notification 엔티티를 두 번 저장하려고 시도
2. **트랜잭션 경계 불일치**: `REQUIRES_NEW`로 새 트랜잭션에서 실행되어 detached 상태의 엔티티를 merge 시도
3. **Version 충돌**: 첫 번째 저장 후 version이 증가했는데, 두 번째 저장 시 이전 version으로 저장 시도

## 해결 방안

### 방안 1: 엔티티 재조회 후 업데이트
```java
@Override
public Notification sendNotification(Notification notification) throws MessagingException {
    // ... 발송 로직 ...
    
    // 엔티티 재조회
    Notification freshNotification = notificationRepository
        .findNotificationById(notification.getNotificationId())
        .orElseThrow();
    
    // 상태만 업데이트
    freshNotification.updateStatus(sendNotification.getStatus());
    freshNotification.setSentAt(LocalDateTime.now());
    
    // 저장
    return notificationRepository.saveNotification(freshNotification);
}
```

### 방안 2: 트랜잭션 전파 수정
```java
@Async
@EventListener
@Transactional(propagation = Propagation.REQUIRED) // REQUIRES_NEW 대신 REQUIRED 사용
public void on(NotificationCreated event) {
    // 같은 트랜잭션 내에서 처리
    notificationUseCase.sendNotification(event.notification());
}
```

### 방안 3: 상태 업데이트 전용 메서드 추가
```java
public interface NotificationRepository {
    void updateNotificationStatus(String notificationId, NotificationStatus status, LocalDateTime sentAt);
}
```

## 현재 상황
- **영향**: 이메일은 정상 발송되지만 DB 상태 업데이트 실패
- **우선순위**: 중간 (기능은 동작하지만 에러 로그 발생)
- **임시 대응**: 에러는 발생하지만 실제 이메일 발송은 정상 동작

## TODO
- [ ] 해결 방안 선택 및 구현
- [ ] 테스트 케이스 추가
- [ ] 동시성 테스트 수행