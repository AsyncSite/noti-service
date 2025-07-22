package com.asyncsite.notiservice.adapter.in.mapper;

import com.asyncsite.notiservice.adapter.in.dto.*;
import com.asyncsite.notiservice.domain.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationMapper {
    
    // === Response 매핑 ===
    
    public static NotificationResponse toResponse(Notification notification) {
        List<NotificationChannelResponse> channelResponses = notification.getChannels() != null ?
                notification.getChannels().stream()
                        .map(NotificationMapper::toChannelResponse)
                        .toList() : List.of();

        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getUserId(),
                notification.getEventType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getStatus().name(),
                notification.getMetadata(),
                channelResponses,
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getRetryCount(),
                notification.getErrorMessage()
        );
    }

    public static NotificationChannelResponse toChannelResponse(NotificationChannel channel) {
        return new NotificationChannelResponse(
                channel.getChannelId(),
                channel.getNotificationId(),
                channel.getChannelType().name(),
                channel.getRecipient(),
                channel.getStatus().name(),
                channel.getSentAt(),
                channel.getExternalId(),
                channel.getResponseData(),
                channel.getCreatedAt(),
                channel.getUpdatedAt(),
                channel.getErrorMessage(),
                channel.getRetryCount(),
                channel.getLastRetryAt(),
                null // TODO: 필요시 deliveryHistory 매핑 구현
        );
    }

    // === Request에서 Domain 매핑 ===
    
    public static NotificationTemplate toNotificationTemplate(NotificationTemplateRequest request) {
        NotificationChannel.ChannelType channelType = NotificationChannel.ChannelType.valueOf(request.channelType().toUpperCase());
        
        Map<String, String> variableMap = request.variables() != null ?
                request.variables().stream()
                        .collect(Collectors.toMap(v -> v, v -> "")) : Map.of();

        return NotificationTemplate.create(
                request.eventType(),
                channelType,
                request.language(),
                request.titleTemplate(),
                request.contentTemplate(),
                variableMap
        );
    }

    public static NotificationSettings toNotificationSettings(String userId, NotificationSettingsRequest request) {
        return NotificationSettings.create(
                userId,
                request.studyUpdates(),
                request.marketing(),
                request.emailEnabled(),
                request.discordEnabled(),
                request.pushEnabled(),
                request.timezone(),
                request.language(),
                request.quietHours()
        );
    }

    // === 업데이트 매핑 ===
    
    public static NotificationTemplate updateNotificationTemplate(NotificationTemplate existing, NotificationTemplateRequest request) {
        Map<String, String> variableMap = request.variables() != null ?
                request.variables().stream()
                        .collect(Collectors.toMap(v -> v, v -> "")) : Map.of();

        return existing.updateTemplate(
                request.titleTemplate(),
                request.contentTemplate(),
                variableMap
        );
    }

    public static NotificationSettings updateNotificationSettings(NotificationSettings existing, NotificationSettingsRequest request) {
        // 도메인 행위 메서드들을 체이닝하여 사용
        NotificationSettings updated = existing
                .updateEventSettings(request.studyUpdates(), request.marketing())
                .updateChannelEnabled(NotificationChannel.ChannelType.EMAIL, request.emailEnabled())
                .updateChannelEnabled(NotificationChannel.ChannelType.DISCORD, request.discordEnabled())
                .updateChannelEnabled(NotificationChannel.ChannelType.PUSH, request.pushEnabled())
                .updateLocalizationSettings(request.timezone(), request.language());

        // 방해금지 시간이 변경된 경우에만 업데이트
        if (request.quietHours() != null && !request.quietHours().equals(existing.getQuietHours())) {
            updated = updated.updateQuietHours(request.quietHours());
        }

        return updated;
    }
} 