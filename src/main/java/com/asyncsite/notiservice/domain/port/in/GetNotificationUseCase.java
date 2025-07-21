package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.Notification;

import java.util.List;
import java.util.Optional;

public interface GetNotificationUseCase {
    
    /**
     * 알림 ID로 알림을 조회합니다.
     * 
     * @param notificationId 알림 ID
     * @return 알림 정보 (없으면 empty)
     */
    Optional<Notification> getNotificationById(String notificationId);
    
    /**
     * 사용자의 알림 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 알림 목록
     */
    List<Notification> getNotificationsByUserId(String userId, int page, int size);
} 