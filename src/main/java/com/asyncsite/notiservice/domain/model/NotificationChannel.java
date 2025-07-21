package com.asyncsite.notiservice.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder(toBuilder = true)
public class NotificationChannel {
    private String channelId;
    private String notificationId;
    private ChannelType channelType;
    private String recipient;
    private Status status;
    private LocalDateTime sentAt;
    private String externalId;
    private Map<String, Object> responseData;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime lastRetryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ChannelType {
        EMAIL, DISCORD, PUSH
    }

    public enum Status {
        PENDING, SENT, FAILED, RETRY
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    public boolean isSent() {
        return status == Status.SENT;
    }

    public boolean isFailed() {
        return status == Status.FAILED;
    }

    public boolean canRetry() {
        return status == Status.FAILED;
    }

    public NotificationChannel withStatus(Status newStatus) {
        return NotificationChannel.builder()
                .channelId(this.channelId)
                .notificationId(this.notificationId)
                .channelType(this.channelType)
                .recipient(this.recipient)
                .status(newStatus)
                .sentAt(this.sentAt)
                .externalId(this.externalId)
                .responseData(this.responseData)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public NotificationChannel withSentAt(LocalDateTime sentAt) {
        return NotificationChannel.builder()
                .channelId(this.channelId)
                .notificationId(this.notificationId)
                .channelType(this.channelType)
                .recipient(this.recipient)
                .status(this.status)
                .sentAt(sentAt)
                .externalId(this.externalId)
                .responseData(this.responseData)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public NotificationChannel withResponseData(Map<String, Object> responseData) {
        return NotificationChannel.builder()
                .channelId(this.channelId)
                .notificationId(this.notificationId)
                .channelType(this.channelType)
                .recipient(this.recipient)
                .status(this.status)
                .sentAt(this.sentAt)
                .externalId(this.externalId)
                .responseData(responseData)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public NotificationChannel withErrorMessage(String errorMessage) {
        return NotificationChannel.builder()
                .channelId(this.channelId)
                .notificationId(this.notificationId)
                .channelType(this.channelType)
                .recipient(this.recipient)
                .status(this.status)
                .sentAt(this.sentAt)
                .externalId(this.externalId)
                .responseData(this.responseData)
                .errorMessage(errorMessage)
                .retryCount(this.retryCount)
                .lastRetryAt(this.lastRetryAt)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
