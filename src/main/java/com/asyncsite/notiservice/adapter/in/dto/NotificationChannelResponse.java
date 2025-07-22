package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.NotificationChannel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record NotificationChannelResponse(
    String channelId,
    String notificationId,
    String channelType,
    String recipient,
    String status,
    LocalDateTime sentAt,
    String externalId,
    Map<String, Object> responseData,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String errorMessage,
    Integer retryCount,
    LocalDateTime lastRetryAt,
    List<DeliveryHistory> deliveryHistory
) {
    public static NotificationChannelResponse from(NotificationChannel channel) {
        return com.asyncsite.notiservice.adapter.in.mapper.NotificationMapper.toChannelResponse(channel);
    }

    public record DeliveryHistory(
        LocalDateTime timestamp,
        String status,
        String details
    ) {}
}
