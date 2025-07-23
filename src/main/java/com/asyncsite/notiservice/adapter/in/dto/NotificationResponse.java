package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record NotificationResponse(
        String notificationId,
        String userId,
        String templateId,
        ChannelType channelType,
        Map<String, Object> metadata,
        NotificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime sentAt,
        Integer retryCount
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getUserId(),
                notification.getTemplateId(),
                notification.getChannelType(),
                notification.getMetadata(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getSentAt(),
                notification.getRetryCount()
        );
    }
}
