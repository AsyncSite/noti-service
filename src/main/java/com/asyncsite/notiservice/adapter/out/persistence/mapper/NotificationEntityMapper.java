package com.asyncsite.notiservice.adapter.out.persistence.mapper;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationEntity;
import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationChannelEntity;
import com.asyncsite.notiservice.domain.model.Notification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class NotificationEntityMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static Notification toDomain(NotificationEntity entity) {
        return Notification.builder()
                .notificationId(entity.getNotificationId())
                .userId(entity.getUserId())
                .eventType(entity.getEventType())
                .title(entity.getTitle())
                .content(entity.getContent())
                .metadata(parseJson(entity.getMetadata()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .sentAt(entity.getSentAt())
                .updatedAt(entity.getUpdatedAt())
                .retryCount(entity.getRetryCount())
                .errorMessage(entity.getErrorMessage())
                .channels(List.of()) // 직접참조: 채널은 별도 조회 필요
                .build();
    }
    public static NotificationEntity toEntity(Notification notification) {
        return NotificationEntity.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .eventType(notification.getEventType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .metadata(writeJson(notification.getMetadata()))
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .updatedAt(notification.getUpdatedAt())
                .retryCount(notification.getRetryCount())
                .errorMessage(notification.getErrorMessage())
                .build();
    }
    private static Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty()) return Map.of();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }
    private static String writeJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
} 