package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.Notification;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface SendNotificationUseCase {
    
    /**
     * 알림을 발송합니다.
     * 
     * @param userId 사용자 ID
     * @param eventType 이벤트 타입
     * @param metadata 메타데이터
     * @return 발송된 알림 정보
     */
    CompletableFuture<Notification> sendNotification(String userId, String eventType, Map<String, Object> metadata);
    
    /**
     * 알림을 재시도합니다.
     * 
     * @param notificationId 알림 ID
     * @return 재시도된 알림 정보
     */
    CompletableFuture<Notification> retryNotification(String notificationId);
} 