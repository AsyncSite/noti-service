package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.model.vo.NotificationSearchCriteria;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * 백오피스용 전체 알림 목록을 조회합니다.
     * 모든 사용자의 알림을 최신순으로 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 알림 목록 페이지
     */
    Page<Notification> getAllNotifications(Pageable pageable);

    /**
     * 백오피스용 알림 검색 기능입니다.
     * 다양한 조건으로 알림을 검색할 수 있습니다.
     *
     * @param criteria 검색 조건
     * @param pageable 페이징 정보
     * @return 검색된 알림 목록 페이지
     */
    Page<Notification> searchNotifications(NotificationSearchCriteria criteria, Pageable pageable);

    /**
     * 예약된 알림을 취소합니다.
     * SCHEDULED 상태인 알림만 취소 가능합니다.
     *
     * @param notificationId 알림 ID
     * @return 취소된 알림 정보
     * @throws IllegalStateException 예약 상태가 아닌 경우
     */
    Notification cancelScheduledNotification(String notificationId);

    /**
     * 이메일 알림의 HTML 미리보기를 렌더링합니다.
     *
     * @param notification 알림 정보
     * @return 렌더링된 HTML 콘텐츠
     */
    String renderEmailPreview(Notification notification);
}
