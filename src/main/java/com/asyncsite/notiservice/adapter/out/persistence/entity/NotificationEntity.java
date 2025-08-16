package com.asyncsite.notiservice.adapter.out.persistence.entity;

import com.asyncsite.notiservice.common.JsonUtil;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    
    @Column(columnDefinition = "TEXT")
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String recipientContactJson;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private String failMessage;
    private Integer retryCount;

    public static NotificationEntity from(Notification notification) {
        return NotificationEntity.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .templateId(notification.getTemplateId())
                .channelType(notification.getChannelType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .recipientContactJson(JsonUtil.toJson(notification.getRecipientContacts()))
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .sentAt(notification.getSentAt())
                .failMessage(notification.getFailMessage())
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
                .recipientContacts(JsonUtil.fromJson(this.recipientContactJson, List.class))
                .status(this.status)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .sentAt(this.sentAt)
                .failMessage(this.failMessage)
                .retryCount(this.retryCount)
                .version(this.version)
                .build();
    }
}
