package com.asyncsite.notiservice.adapter.in.mapper;

import com.asyncsite.notiservice.adapter.in.dto.NotificationChannelResponse;
import com.asyncsite.notiservice.adapter.in.dto.NotificationResponse;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationChannel;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationMapper {
    public static NotificationResponse toResponse(Notification notification) {
        List<com.asyncsite.notiservice.adapter.in.dto.NotificationChannelResponse> channelResponses = notification.getChannels().stream()
                .map(NotificationMapper::toChannelResponse)
                .collect(Collectors.toList());

        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .eventType(notification.getEventType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .status(notification.getStatus().name())
                .metadata(notification.getMetadata())
                .channels(channelResponses)
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .retryCount(notification.getRetryCount())
                .errorMessage(notification.getErrorMessage())
                .build();
    }

    public static NotificationChannelResponse toChannelResponse(NotificationChannel channel) {
        return NotificationChannelResponse.builder()
                .channelId(channel.getChannelId())
                .notificationId(channel.getNotificationId())
                .channelType(channel.getChannelType().name())
                .recipient(channel.getRecipient())
                .status(channel.getStatus().name())
                .sentAt(channel.getSentAt())
                .externalId(channel.getExternalId())
                .responseData(channel.getResponseData())
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .errorMessage(channel.getErrorMessage())
                .retryCount(channel.getRetryCount())
                .lastRetryAt(channel.getLastRetryAt())
                .deliveryHistory(null) // TODO: 필요시 매핑 구현
                .build();
    }
} 