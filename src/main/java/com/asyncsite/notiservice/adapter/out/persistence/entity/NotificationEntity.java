package com.asyncsite.notiservice.adapter.out.persistence.entity;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @Column(name = "notification_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String notificationId;
    @Version
    private Long version;
    private String userId;
    private String templateId;
    private ChannelType channelType;
    private String title;
    private String content;
    private String recipientContact;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private Integer retryCount;

    public static NotificationEntity from(Notification notification) {
        return NotificationEntity.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .templateId(notification.getTemplateId())
                .channelType(notification.getChannelType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .recipientContact(notification.getRecipientContact())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .sentAt(notification.getSentAt())
                .retryCount(notification.getRetryCount())
                .version(notification.getVersion())
                .build();
    }

    public Notification toDomain() {
        return Notification.builder()
                .notificationId(this.notificationId)
                .userId(this.userId)
                .templateId(this.templateId)
                .channelType(this.channelType)
                .title(this.title)
                .content(this.content)
                .recipientContact(this.recipientContact)
                .status(this.status)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .sentAt(this.sentAt)
                .retryCount(this.retryCount)
                .version(this.version)
                .build();
    }
}
