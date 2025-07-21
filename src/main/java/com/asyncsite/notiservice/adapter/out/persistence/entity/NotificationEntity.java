package com.asyncsite.notiservice.adapter.out.persistence.entity;

import com.asyncsite.notiservice.domain.model.Notification;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @Column(name = "notification_id")
    private String notificationId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Notification.NotificationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<NotificationChannelEntity> channels;
    // → 직접참조 방식으로 제거 또는 필요시 List<Long> channelIds 등으로 대체

    public static NotificationEntity from(Notification notification) {
        return NotificationEntity.builder()
                .notificationId(Strings.isEmpty(notification.getNotificationId()) ? UUID.randomUUID().toString() : notification.getNotificationId())
                .userId(notification.getUserId())
                .eventType(notification.getEventType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .metadata(notification.getMetadata() != null ? notification.getMetadata().toString() : null)
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .updatedAt(notification.getUpdatedAt())
                .retryCount(notification.getRetryCount())
                .errorMessage(notification.getErrorMessage())
                .build();
    }

    public Notification toDomain() {
        return Notification.builder()
                .notificationId(this.notificationId)
                .userId(this.userId)
                .eventType(this.eventType)
                .title(this.title)
                .content(this.content)
                .metadata(this.metadata != null ? Map.of() : null) // TODO: JSON 파싱 구현
                .status(this.status)
                .createdAt(this.createdAt)
                .sentAt(this.sentAt)
                .updatedAt(this.updatedAt)
                .retryCount(this.retryCount)
                .errorMessage(this.errorMessage)
                .channels(List.of()) // 직접참조 방식: 채널은 별도 조회 필요
                .build();
    }
}
