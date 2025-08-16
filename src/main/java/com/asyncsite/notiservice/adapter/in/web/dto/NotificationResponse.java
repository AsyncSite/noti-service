package com.asyncsite.notiservice.adapter.in.web.dto;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationResponse(
        String notificationId,
        String userId,
        String templateId,
        ChannelType channelType,
        String title,
        String content,
        List<String> recipientContacts,
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
                notification.getTitle(),
                notification.getContent(),
                notification.getRecipientContacts(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getSentAt(),
                notification.getRetryCount()
        );
    }
}
