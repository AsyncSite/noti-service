package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
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

    Notification sendNotification(Notification notification) throws MessagingException, UnsupportedEncodingException;

    /**
     * 알림을 재시도합니다.
     *
     * @param notificationId 알림 ID
     * @return 재시도된 알림 정보
     */
    Notification retryNotification(String notificationId) throws MessagingException, UnsupportedEncodingException;

    /**
     * 예약된 알림을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param channelType 발송 타입
     * @param eventType 이벤트 타입
     * @param metadata 메타데이터
     * @param recipientContact 수신자 연락처
     * @param scheduledAt 예약 발송 시간
     * @return 생성된 예약 알림
     */
    Notification createScheduledNotification(String userId, ChannelType channelType, EventType eventType,
                                           Map<String, Object> metadata, String recipientContact,
                                           LocalDateTime scheduledAt);

    /**
     * 예약된 알림을 대량으로 생성합니다.
     *
     * @param userId 사용자 ID
     * @param channelType 발송 타입
     * @param eventType 이벤트 타입
     * @param metadata 메타데이터
     * @param recipientContacts 수신자 연락처 목록
     * @param scheduledAt 예약 발송 시간
     * @return 생성된 예약 알림
     */
    Notification createScheduledNotificationBulk(String userId, ChannelType channelType, EventType eventType,
                                                Map<String, Object> metadata, List<String> recipientContacts,
                                                LocalDateTime scheduledAt);

    /**
     * 알림 설정을 무시하고 강제로 알림을 발송합니다.
     *
     * @param userId 사용자 ID
     * @param channelType 발송 타입
     * @param eventType 이벤트 타입
     * @param metadata 메타데이터
     * @param recipientContact 수신자 연락처
     * @return 발송된 알림 정보
     */
    Notification createForceNotification(String userId, ChannelType channelType, EventType eventType,
                                        Map<String, Object> metadata, String recipientContact);

    /**
     * 알림 설정을 무시하고 강제로 예약 알림을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param channelType 발송 타입
     * @param eventType 이벤트 타입
     * @param metadata 메타데이터
     * @param recipientContact 수신자 연락처
     * @param scheduledAt 예약 발송 시간
     * @return 생성된 예약 알림
     */
    Notification createForceScheduledNotification(String userId, ChannelType channelType, EventType eventType,
                                                 Map<String, Object> metadata, String recipientContact,
                                                 LocalDateTime scheduledAt);
}
