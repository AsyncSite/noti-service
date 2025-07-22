package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record NotificationResponse(
    String notificationId,
    String userId,
    String eventType,
    String title,
    String content,
    String status,
    Map<String, Object> metadata,
    List<NotificationChannelResponse> channels,
    LocalDateTime createdAt,
    LocalDateTime sentAt,
    Integer retryCount,
    String errorMessage
) {
    public static NotificationResponse from(Notification notification) {
        return com.asyncsite.notiservice.adapter.in.mapper.NotificationMapper.toResponse(notification);
    }
}
