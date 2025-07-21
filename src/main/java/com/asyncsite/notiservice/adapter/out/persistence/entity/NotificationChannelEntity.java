package com.asyncsite.notiservice.adapter.out.persistence.entity;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notification_channels")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationChannelEntity {

    @Id
    @Column(name = "channel_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String channelId;

    @Column(name = "notification_id", nullable = false)
    private String notificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false)
    private NotificationChannel.ChannelType channelType;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationChannel.Status status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "response_data", columnDefinition = "JSON")
    private String responseData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static NotificationChannelEntity from(NotificationChannel channel) {
        return NotificationChannelEntity.builder()
                .channelId(Strings.isEmpty(channel.getChannelId()) ? UUID.randomUUID().toString() : channel.getChannelId())
                .notificationId(channel.getNotificationId())
                .channelType(channel.getChannelType())
                .recipient(channel.getRecipient())
                .status(channel.getStatus())
                .sentAt(channel.getSentAt())
                .externalId(channel.getExternalId())
                .responseData(writeJson(channel.getResponseData()))
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .build();
    }

    public NotificationChannel toDomain() {
        return NotificationChannel.builder()
                .channelId(this.channelId)
                .notificationId(this.notificationId)
                .channelType(this.channelType)
                .recipient(this.recipient)
                .status(this.status)
                .sentAt(this.sentAt)
                .externalId(this.externalId)
                .responseData(parseJson(this.responseData))
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();
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
