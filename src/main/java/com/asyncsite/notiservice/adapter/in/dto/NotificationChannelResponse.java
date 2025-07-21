package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class NotificationChannelResponse {

    private final String channelId;
    private final String notificationId;
    private final String channelType;
    private final String recipient;
    private final String status;
    private final LocalDateTime sentAt;
    private final String externalId;
    private final Map<String, Object> responseData;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String errorMessage;
    private final Integer retryCount;
    private final LocalDateTime lastRetryAt;
    private final List<DeliveryHistory> deliveryHistory;

    public static NotificationChannelResponse from(NotificationChannel channel) {
        return com.asyncsite.notiservice.adapter.in.mapper.NotificationMapper.toChannelResponse(channel);
    }

    @Getter
    @Builder
    public static class DeliveryHistory {
        private final LocalDateTime timestamp;
        private final String status;
        private final String details;
    }
}
