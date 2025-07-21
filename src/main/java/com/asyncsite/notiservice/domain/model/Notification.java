package com.asyncsite.notiservice.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder(toBuilder = true)
public class Notification {
    private String notificationId;
    private String userId;
    private String eventType;
    private String title;
    private String content;
    private Map<String, Object> metadata;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime updatedAt;
    private Integer retryCount;
    private String errorMessage;
    private List<NotificationChannel> channels;

    public enum NotificationStatus {
        PENDING, SENT, FAILED, RETRY
    }

    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }

    public boolean isSent() {
        return status == NotificationStatus.SENT;
    }

    public boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }

    public boolean canRetry() {
        return status == NotificationStatus.FAILED && retryCount < 3;
    }

    public Notification withStatus(NotificationStatus newStatus) {
        return Notification.builder()
                .notificationId(this.notificationId)
                .userId(this.userId)
                .eventType(this.eventType)
                .title(this.title)
                .content(this.content)
                .metadata(this.metadata)
                .status(newStatus)
                .createdAt(this.createdAt)
                .sentAt(this.sentAt)
                .updatedAt(LocalDateTime.now())
                .retryCount(this.retryCount)
                .errorMessage(this.errorMessage)
                .channels(this.channels)
                .build();
    }

    public Notification withRetryCount(int newRetryCount) {
        return Notification.builder()
                .notificationId(this.notificationId)
                .userId(this.userId)
                .eventType(this.eventType)
                .title(this.title)
                .content(this.content)
                .metadata(this.metadata)
                .status(this.status)
                .createdAt(this.createdAt)
                .sentAt(this.sentAt)
                .updatedAt(LocalDateTime.now())
                .retryCount(newRetryCount)
                .errorMessage(this.errorMessage)
                .channels(this.channels)
                .build();
    }

    public Notification withErrorMessage(String errorMessage) {
        return Notification.builder()
                .notificationId(this.notificationId)
                .userId(this.userId)
                .eventType(this.eventType)
                .title(this.title)
                .content(this.content)
                .metadata(this.metadata)
                .status(this.status)
                .createdAt(this.createdAt)
                .sentAt(this.sentAt)
                .updatedAt(LocalDateTime.now())
                .retryCount(this.retryCount)
                .errorMessage(errorMessage)
                .channels(this.channels)
                .build();
    }

    public Notification withSentAt(LocalDateTime sentAt) {
        return Notification.builder()
                .notificationId(this.notificationId)
                .userId(this.userId)
                .eventType(this.eventType)
                .title(this.title)
                .content(this.content)
                .metadata(this.metadata)
                .status(this.status)
                .createdAt(this.createdAt)
                .sentAt(sentAt)
                .updatedAt(LocalDateTime.now())
                .retryCount(this.retryCount)
                .errorMessage(this.errorMessage)
                .channels(this.channels)
                .build();
    }
}
