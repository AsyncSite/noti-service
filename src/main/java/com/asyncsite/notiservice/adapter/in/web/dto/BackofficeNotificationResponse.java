package com.asyncsite.notiservice.adapter.in.web.dto;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 백오피스 알림 목록 조회용 응답 DTO
 * 목록 표시에 필요한 핵심 정보만 포함
 */
@Builder
public record BackofficeNotificationResponse(
        String notificationId,
        String userId,
        String templateId,
        ChannelType channelType,
        NotificationStatus status,
        String title,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        LocalDateTime scheduledAt,
        Integer retryCount
) {
    /**
     * 도메인 모델을 백오피스 응답 DTO로 변환
     *
     * @param notification 알림 도메인 모델
     * @return 백오피스 응답 DTO
     */
    public static BackofficeNotificationResponse from(Notification notification) {
        return BackofficeNotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .templateId(notification.getTemplateId())
                .channelType(notification.getChannelType())
                .status(notification.getStatus())
                .title(notification.getTitle())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .scheduledAt(notification.getScheduledAt())
                .retryCount(notification.getRetryCount())
                .build();
    }
}