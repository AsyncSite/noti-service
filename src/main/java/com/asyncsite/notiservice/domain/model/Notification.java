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
    private LocalDateTime scheduledAt;


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

    /**
     * 예약된 알림을 생성합니다.
     */
    public static Notification createScheduled(String userId, String templateId, ChannelType channelType,
                                               String title, String content, List<String> recipientContacts,
                                               LocalDateTime scheduledAt) {
        LocalDateTime now = LocalDateTime.now();
        NotificationStatus status = (scheduledAt != null && scheduledAt.isAfter(now))
                ? NotificationStatus.SCHEDULED
                : NotificationStatus.PENDING;

        return Notification.builder()
                .userId(userId)
                .templateId(templateId)
                .channelType(channelType)
                .title(title)
                .content(content)
                .recipientContacts(recipientContacts)
                .status(status)
                .scheduledAt(scheduledAt)
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

    /**
     * 알림 상태를 업데이트합니다.
     */
    public void updateStatus(NotificationStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
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
     * 알림이 PENDING 상태로 너무 오래 머물러 있는지 확인합니다.
     * 5분 이상 PENDING 상태인 경우 true를 반환합니다.
     */
    public boolean hasBeenPendingTooLong() {
        if (!isPending()) {
            return false;
        }
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return updatedAt != null && updatedAt.isBefore(fiveMinutesAgo);
    }

    /**
     * 알림이 처리 중인지 확인합니다.
     */
    public boolean isProcessing() {
        return isPending() || isRetry();
    }

    public boolean isScheduled() {
        return status == NotificationStatus.SCHEDULED;
    }

    public boolean shouldBeSentNow() {
        return isScheduled() && scheduledAt != null && !scheduledAt.isAfter(LocalDateTime.now());
    }

    /**
     * 예약된 알림을 취소합니다.
     * SCHEDULED 상태에서만 호출 가능합니다.
     */
    public void cancel() {
        if (status != NotificationStatus.SCHEDULED) {
            throw new IllegalStateException("예약 상태의 알림만 취소할 수 있습니다. 현재 상태: " + status);
        }
        this.status = NotificationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 알림이 취소되었는지 확인합니다.
     */
    public boolean isCancelled() {
        return status == NotificationStatus.CANCELLED;
    }
}
