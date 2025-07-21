package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationChannel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class NotificationResponse {

    private String notificationId;

    private String userId;

    private String eventType;

    private String title;

    private String content;

    private String status;



    private Map<String, Object> metadata;

    private List<com.asyncsite.notiservice.adapter.in.dto.NotificationChannelResponse> channels;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    private Integer retryCount;

    private String errorMessage;

    public static NotificationResponse from(Notification notification) {
        return com.asyncsite.notiservice.adapter.in.mapper.NotificationMapper.toResponse(notification);
    }
}
