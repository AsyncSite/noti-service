# Notification Service API Specification

## 🚀 API Overview

알림 서비스의 4개 핵심 도메인에 대한 REST API 명세서입니다.

---

## 📧 1. Notification APIs

### 1.1 알림 발송
```http
POST /api/v1/notifications
Content-Type: application/json

{
  "userId": 123,
  "eventType": "STUDY_APPROVED",
  "title": "스터디 승인 알림",
  "content": "귀하의 스터디가 승인되었습니다.",
  "metadata": {
    "studyId": 456,
    "studyTitle": "Java Spring Boot Study",
    "approvedBy": "Admin"
  },
  "channels": ["EMAIL", "DISCORD"],
  "priority": "HIGH",
  "scheduledAt": "2024-07-20T09:00:00Z"
}
```

**Response (201 Created):**
```json
{
  "notificationId": 789,
  "status": "PROCESSING",
  "channels": ["EMAIL", "DISCORD"],
  "createdAt": "2024-07-19T10:30:00Z",
  "estimatedDelivery": "2024-07-19T10:31:00Z"
}
```

### 1.2 알림 상세 조회
```http
GET /api/v1/notifications/{notificationId}
```

**Response (200 OK):**
```json
{
  "notificationId": 789,
  "userId": 123,
  "eventType": "STUDY_APPROVED",
  "title": "스터디 승인 알림",
  "content": "귀하의 스터디가 승인되었습니다.",
  "status": "SENT",
  "priority": "HIGH",
  "metadata": {
    "studyId": 456,
    "studyTitle": "Java Spring Boot Study"
  },
  "channels": [
    {
      "channelType": "EMAIL",
      "status": "SENT",
      "sentAt": "2024-07-19T10:30:45Z",
      "recipient": "user@example.com"
    },
    {
      "channelType": "DISCORD",
      "status": "FAILED",
      "errorMessage": "Webhook not found",
      "retryCount": 2
    }
  ],
  "createdAt": "2024-07-19T10:30:00Z",
  "sentAt": "2024-07-19T10:30:45Z"
}
```

### 1.3 사용자별 알림 목록 조회
```http
GET /api/v1/notifications?userId=123&page=0&size=20&sort=createdAt,desc&status=SENT&eventType=STUDY_APPROVED
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "notificationId": 789,
      "eventType": "STUDY_APPROVED",
      "title": "스터디 승인 알림",
      "status": "SENT",
      "createdAt": "2024-07-19T10:30:00Z",
      "channelCount": 2,
      "successfulChannels": 1
    }
  ],
  "pageable": {
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### 1.4 알림 재시도
```http
POST /api/v1/notifications/{notificationId}/retry
```

**Response (200 OK):**
```json
{
  "notificationId": 789,
  "retryCount": 3,
  "status": "RETRY_SCHEDULED",
  "nextRetryAt": "2024-07-19T11:00:00Z"
}
```

### 1.5 알림 취소
```http
DELETE /api/v1/notifications/{notificationId}
```

**Response (200 OK):**
```json
{
  "message": "Notification cancelled successfully",
  "notificationId": 789,
  "cancelledAt": "2024-07-19T10:35:00Z"
}
```

### 1.6 대량 알림 발송
```http
POST /api/v1/notifications/bulk
Content-Type: application/json

{
  "userIds": [123, 124, 125],
  "eventType": "SYSTEM_MAINTENANCE",
  "title": "시스템 점검 안내",
  "content": "오늘 밤 시스템 점검이 예정되어 있습니다.",
  "channels": ["EMAIL"],
  "scheduledAt": "2024-07-19T18:00:00Z"
}
```

---

## 📱 2. NotificationChannel APIs

### 2.1 채널 목록 조회
```http
GET /api/v1/notification-channels?notificationId=789&channelType=EMAIL
```

**Response (200 OK):**
```json
{
  "channels": [
    {
      "channelId": 1001,
      "notificationId": 789,
      "channelType": "EMAIL",
      "recipient": "user@example.com",
      "status": "SENT",
      "sentAt": "2024-07-19T10:30:45Z",
      "externalId": "msg_1234567890",
      "responseData": {
        "messageId": "0000014a-f4d4-4f36-82c5-6a4f2e8b9c1a",
        "status": "delivered"
      }
    },
    {
      "channelId": 1002,
      "notificationId": 789,
      "channelType": "DISCORD",
      "recipient": "webhook_url",
      "status": "FAILED",
      "errorMessage": "Webhook not found",
      "retryCount": 2,
      "lastRetryAt": "2024-07-19T10:32:00Z"
    }
  ]
}
```

### 2.2 채널별 상세 조회
```http
GET /api/v1/notification-channels/{channelId}
```

**Response (200 OK):**
```json
{
  "channelId": 1001,
  "notificationId": 789,
  "channelType": "EMAIL",
  "recipient": "user@example.com",
  "status": "SENT",
  "sentAt": "2024-07-19T10:30:45Z",
  "externalId": "msg_1234567890",
  "responseData": {
    "messageId": "0000014a-f4d4-4f36-82c5-6a4f2e8b9c1a",
    "status": "delivered",
    "openedAt": "2024-07-19T11:15:23Z",
    "clickedAt": "2024-07-19T11:16:10Z"
  },
  "deliveryHistory": [
    {
      "timestamp": "2024-07-19T10:30:45Z",
      "status": "SENT",
      "details": "Email sent successfully"
    },
    {
      "timestamp": "2024-07-19T10:31:12Z",
      "status": "DELIVERED",
      "details": "Email delivered to recipient"
    }
  ]
}
```

### 2.3 채널 재시도
```http
POST /api/v1/notification-channels/{channelId}/retry
```

**Response (200 OK):**
```json
{
  "channelId": 1002,
  "status": "RETRY_SCHEDULED",
  "retryCount": 3,
  "nextRetryAt": "2024-07-19T11:00:00Z"
}
```

### 2.4 채널 통계 조회
```http
GET /api/v1/notification-channels/statistics?startDate=2024-07-01&endDate=2024-07-19&channelType=EMAIL
```

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2024-07-01",
    "endDate": "2024-07-19"
  },
  "channelType": "EMAIL",
  "statistics": {
    "totalSent": 1250,
    "successful": 1180,
    "failed": 70,
    "successRate": 94.4,
    "avgDeliveryTime": "00:00:45",
    "bounceRate": 2.1,
    "openRate": 68.5,
    "clickRate": 12.3
  },
  "dailyStats": [
    {
      "date": "2024-07-19",
      "sent": 85,
      "successful": 82,
      "failed": 3
    }
  ]
}
```

---

## 📝 3. NotificationTemplate APIs

### 3.1 템플릿 목록 조회
```http
GET /api/v1/notification-templates?eventType=STUDY_APPROVED&channelType=EMAIL&language=ko&isActive=true&page=0&size=10
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "templateId": 101,
      "eventType": "STUDY_APPROVED",
      "channelType": "EMAIL",
      "language": "ko",
      "title": "스터디 승인 알림",
      "titleTemplate": "{{studyTitle}} 스터디가 승인되었습니다",
      "contentTemplate": "안녕하세요 {{userName}}님,\n\n귀하가 신청한 {{studyTitle}} 스터디가 승인되었습니다.",
      "variables": ["userName", "studyTitle", "approvedBy"],
      "isActive": true,
      "createdAt": "2024-07-01T09:00:00Z",
      "updatedAt": "2024-07-15T14:30:00Z"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3
  }
}
```

### 3.2 템플릿 상세 조회
```http
GET /api/v1/notification-templates/{templateId}
```

**Response (200 OK):**
```json
{
  "templateId": 101,
  "eventType": "STUDY_APPROVED",
  "channelType": "EMAIL",
  "language": "ko",
  "title": "스터디 승인 알림",
  "titleTemplate": "{{studyTitle}} 스터디가 승인되었습니다",
  "contentTemplate": "안녕하세요 {{userName}}님,\n\n귀하가 신청한 {{studyTitle}} 스터디가 승인되었습니다.\n\n승인자: {{approvedBy}}\n승인일시: {{approvedAt}}\n\n스터디 참여: {{studyUrl}}",
  "variables": ["userName", "studyTitle", "approvedBy", "approvedAt", "studyUrl"],
  "isActive": true,
  "version": 2,
  "createdAt": "2024-07-01T09:00:00Z",
  "updatedAt": "2024-07-15T14:30:00Z",
  "createdBy": "admin",
  "updatedBy": "admin"
}
```

### 3.3 템플릿 생성
```http
POST /api/v1/notification-templates
Content-Type: application/json

{
  "eventType": "STUDY_REJECTED",
  "channelType": "EMAIL",
  "language": "ko",
  "titleTemplate": "{{studyTitle}} 스터디 신청이 반려되었습니다",
  "contentTemplate": "안녕하세요 {{userName}}님,\n\n귀하가 신청한 {{studyTitle}} 스터디가 반려되었습니다.\n\n반려 사유: {{rejectionReason}}\n\n문의사항이 있으시면 고객센터로 연락해주세요.",
  "variables": ["userName", "studyTitle", "rejectionReason"],
  "isActive": true
}
```

**Response (201 Created):**
```json
{
  "templateId": 102,
  "eventType": "STUDY_REJECTED",
  "channelType": "EMAIL",
  "language": "ko",
  "titleTemplate": "{{studyTitle}} 스터디 신청이 반려되었습니다",
  "contentTemplate": "안녕하세요 {{userName}}님,\n\n귀하가 신청한 {{studyTitle}} 스터디가 반려되었습니다.\n\n반려 사유: {{rejectionReason}}\n\n문의사항이 있으시면 고객센터로 연락해주세요.",
  "variables": ["userName", "studyTitle", "rejectionReason"],
  "isActive": true,
  "version": 1,
  "createdAt": "2024-07-19T10:30:00Z"
}
```

### 3.4 템플릿 수정
```http
PUT /api/v1/notification-templates/{templateId}
Content-Type: application/json

{
  "titleTemplate": "{{studyTitle}} 스터디 신청이 반려되었습니다 (업데이트)",
  "contentTemplate": "안녕하세요 {{userName}}님,\n\n귀하가 신청한 {{studyTitle}} 스터디가 반려되었습니다.\n\n반려 사유: {{rejectionReason}}\n재신청 가능일: {{reapplyDate}}\n\n문의사항이 있으시면 고객센터로 연락해주세요.",
  "variables": ["userName", "studyTitle", "rejectionReason", "reapplyDate"],
  "isActive": true
}
```

### 3.5 템플릿 미리보기
```http
POST /api/v1/notification-templates/{templateId}/preview
Content-Type: application/json

{
  "variables": {
    "userName": "홍길동",
    "studyTitle": "Java Spring Boot 마스터 클래스",
    "rejectionReason": "정원 초과",
    "reapplyDate": "2024-08-01"
  }
}
```

**Response (200 OK):**
```json
{
  "title": "Java Spring Boot 마스터 클래스 스터디 신청이 반려되었습니다 (업데이트)",
  "content": "안녕하세요 홍길동님,\n\n귀하가 신청한 Java Spring Boot 마스터 클래스 스터디가 반려되었습니다.\n\n반려 사유: 정원 초과\n재신청 가능일: 2024-08-01\n\n문의사항이 있으시면 고객센터로 연락해주세요.",
  "previewedAt": "2024-07-19T10:30:00Z"
}
```

### 3.6 템플릿 비활성화
```http
PATCH /api/v1/notification-templates/{templateId}/deactivate
```

### 3.7 템플릿 복제
```http
POST /api/v1/notification-templates/{templateId}/clone
Content-Type: application/json

{
  "language": "en",
  "titleTemplate": "Your {{studyTitle}} study application has been rejected",
  "contentTemplate": "Hello {{userName}},\n\nYour application for {{studyTitle}} study has been rejected.\n\nReason: {{rejectionReason}}"
}
```

---

## ⚙️ 4. NotificationSettings APIs

### 4.1 사용자 알림 설정 조회
```http
GET /api/v1/users/{userId}/notification-settings
```

**Response (200 OK):**
```json
{
  "userId": 123,
  "studyUpdates": true,
  "marketing": false,
  "emailEnabled": true,
  "discordEnabled": true,
  "pushEnabled": false,
  "timezone": "Asia/Seoul",
  "language": "ko",
  "quietHours": {
    "enabled": true,
    "startTime": "22:00",
    "endTime": "08:00",
    "weekendsOnly": false
  },
  "channelSettings": {
    "EMAIL": {
      "enabled": true,
      "events": ["STUDY_APPROVED", "STUDY_REJECTED", "STUDY_UPDATED"]
    },
    "DISCORD": {
      "enabled": true,
      "webhookUrl": "https://discord.com/api/webhooks/...",
      "events": ["STUDY_APPROVED"]
    }
  },
  "createdAt": "2024-07-01T09:00:00Z",
  "updatedAt": "2024-07-15T14:30:00Z"
}
```

### 4.2 사용자 알림 설정 업데이트
```http
PUT /api/v1/users/{userId}/notification-settings
Content-Type: application/json

{
  "studyUpdates": true,
  "marketing": false,
  "emailEnabled": true,
  "discordEnabled": false,
  "pushEnabled": true,
  "timezone": "Asia/Seoul",
  "language": "ko",
  "quietHours": {
    "enabled": true,
    "startTime": "23:00",
    "endTime": "07:00",
    "weekendsOnly": true
  },
  "channelSettings": {
    "EMAIL": {
      "enabled": true,
      "events": ["STUDY_APPROVED", "STUDY_REJECTED"]
    },
    "PUSH": {
      "enabled": true,
      "deviceToken": "fGHtY8xQR...",
      "events": ["STUDY_APPROVED"]
    }
  }
}
```

**Response (200 OK):**
```json
{
  "userId": 123,
  "message": "Notification settings updated successfully",
  "updatedAt": "2024-07-19T10:30:00Z",
  "changedFields": ["discordEnabled", "pushEnabled", "quietHours", "channelSettings"]
}
```

### 4.3 특정 이벤트 알림 설정 토글
```http
PATCH /api/v1/users/{userId}/notification-settings/events/{eventType}
Content-Type: application/json

{
  "enabled": false,
  "channels": ["EMAIL"]
}
```

### 4.4 알림 설정 초기화
```http
POST /api/v1/users/{userId}/notification-settings/reset
```

**Response (200 OK):**
```json
{
  "userId": 123,
  "message": "Notification settings reset to default values",
  "resetAt": "2024-07-19T10:30:00Z"
}
```

### 4.5 대량 사용자 설정 조회
```http
GET /api/v1/notification-settings/bulk?userIds=123,124,125&eventType=STUDY_APPROVED
```

**Response (200 OK):**
```json
{
  "settings": [
    {
      "userId": 123,
      "emailEnabled": true,
      "discordEnabled": false,
      "allowedChannels": ["EMAIL"]
    },
    {
      "userId": 124,
      "emailEnabled": true,
      "discordEnabled": true,
      "allowedChannels": ["EMAIL", "DISCORD"]
    }
  ]
}
```

---

## 📊 5. Analytics & Monitoring APIs

### 5.1 알림 대시보드 통계
```http
GET /api/v1/notifications/dashboard?startDate=2024-07-01&endDate=2024-07-19
```

### 5.2 시스템 헬스체크
```http
GET /api/v1/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "connectionPool": "8/10 active connections"
      }
    },
    "emailService": {
      "status": "UP",
      "details": {
        "smtpConnection": "Connected"
      }
    },
    "discordService": {
      "status": "UP",
      "details": {
        "webhookStatus": "Active"
      }
    }
  }
}
```

---

## 🔐 Error Responses

### 공통 에러 형식
```json
{
  "timestamp": "2024-07-19T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "path": "/api/v1/notifications",
  "traceId": "abc123def456",
  "details": {
    "field": "userId",
    "rejectedValue": -1,
    "message": "User ID must be positive"
  }
}
```

### 주요 에러 코드
- **400 Bad Request**: 잘못된 요청 파라미터
- **401 Unauthorized**: 인증 실패  
- **403 Forbidden**: 권한 부족
- **404 Not Found**: 리소스를 찾을 수 없음
- **409 Conflict**: 리소스 충돌
- **429 Too Many Requests**: 요청 한도 초과
- **500 Internal Server Error**: 서버 내부 오류
- **503 Service Unavailable**: 서비스 일시 중단