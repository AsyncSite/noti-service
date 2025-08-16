package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import jakarta.mail.MessagingException;

import java.util.List;
import java.util.Map;

public interface NotificationUseCase {
    /**
     * 알림 ID로 알림을 조회합니다.
     *
     * @param notificationId 알림 ID
     * @return 알림 정보 (없으면 empty)
     */
    Notification getNotificationById(String notificationId);

    /**
     * 사용자의 알림 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 알림 목록
     */
    List<Notification> getNotificationsByUserId(String userId, ChannelType channelType, int page, int size);

    /**
     * 알림을 발송합니다.
     *
     * @param userId 사용자 ID
     * @param channelType 발송 타입
     * @param metadata 메타데이터
     * @return 발송된 알림 정보
     */
    Notification createNotification(String userId, ChannelType channelType, EventType eventType, Map<String, Object> metadata, String recipientContact);
    Notification createNotificationBulk(String userId, ChannelType channelType, EventType eventType, Map<String, Object> metadata, List<String> recipientContacts);

    Notification sendNotification(Notification notification) throws MessagingException;

    /**
     * 알림을 재시도합니다.
     *
     * @param notificationId 알림 ID
     * @return 재시도된 알림 정보
     */
    Notification retryNotification(String notificationId) throws MessagingException;
}
