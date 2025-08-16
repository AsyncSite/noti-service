package com.asyncsite.notiservice.domain.model;

import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class Notification {
    private String notificationId;
    private String userId;
    private String templateId;
    private ChannelType channelType;
    private String title;
    private String content;
    private List<String> recipientContacts;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private String failMessage;
    private Integer retryCount;
    private Long version;


    // === 정적 팩토리 메서드 ===

    /**
     * 새로운 알림을 생성합니다.
     */
    public static Notification create(String userId, String templateId, ChannelType channelType, String title, String content, List<String> recipientContacts
    ) {
        LocalDateTime now = LocalDateTime.now();
        return Notification.builder()
                .userId(userId)
                .templateId(templateId)
                .channelType(channelType)
                .title(title)
                .content(content)
                .recipientContacts(recipientContacts)
                .status(NotificationStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .retryCount(0)
                .version(0L)
                .build();
    }

    // === 도메인 행위 메서드 ===

    public void fail(String message) {
        this.status = NotificationStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
        this.failMessage = message;
    }

    /**
     * 알림을 발송 완료로 표시합니다.
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.updatedAt = LocalDateTime.now();
        this.sentAt = this.updatedAt;
    }

    /**
     * 알림 재시도를 준비합니다.
     */
    public void prepareRetry() {
        if (!canRetry()) {
            throw new IllegalStateException("재시도할 수 없는 알림입니다. status=" + status + ", retryCount=" + retryCount);
        }
        this.retryCount = retryCount + 1;
        this.updatedAt = LocalDateTime.now();
        this.sentAt = this.updatedAt;
    }

    // === 비즈니스 로직 메서드 ===

    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }

    public boolean isSent() {
        return status == NotificationStatus.SENT;
    }

    public boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }

    public boolean isRetry() {
        return status == NotificationStatus.RETRY;
    }

    public boolean canRetry() {
        return isFailed() && retryCount < 3;
    }

    /**
     * 알림이 완료된 상태인지 확인합니다 (발송 완료 또는 최종 실패).
     */
    public boolean isCompleted() {
        return isSent() || (isFailed() && !canRetry());
    }

    /**
     * 알림이 처리 중인지 확인합니다.
     */
    public boolean isProcessing() {
        return isPending() || isRetry();
    }
}
