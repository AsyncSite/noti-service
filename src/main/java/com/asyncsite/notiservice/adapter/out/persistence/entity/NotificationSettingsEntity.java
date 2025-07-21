package com.asyncsite.notiservice.adapter.out.persistence.entity;

import com.asyncsite.notiservice.domain.model.NotificationSettings;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notification_settings")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "study_updates", nullable = false)
    private boolean studyUpdates;

    @Column(name = "marketing", nullable = false)
    private boolean marketing;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "discord_enabled", nullable = false)
    private boolean discordEnabled;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "timezone", nullable = false)
    private String timezone;

    @Column(name = "language")
    private String language;

    @Column(name = "quiet_hours", columnDefinition = "JSON")
    private String quietHours;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static NotificationSettingsEntity from(NotificationSettings settings) {
        return NotificationSettingsEntity.builder()
                .userId(Strings.isEmpty(settings.getUserId()) ? UUID.randomUUID().toString() : settings.getUserId())
                .studyUpdates(settings.isStudyUpdates())
                .marketing(settings.isMarketing())
                .emailEnabled(settings.isEmailEnabled())
                .discordEnabled(settings.isDiscordEnabled())
                .pushEnabled(settings.isPushEnabled())
                .timezone(settings.getTimezone())
                .quietHours(settings.getQuietHours() != null ? settings.getQuietHours().toString() : null)
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    public NotificationSettings toDomain() {
        return NotificationSettings.builder()
                .userId(this.userId)
                .studyUpdates(this.studyUpdates)
                .marketing(this.marketing)
                .emailEnabled(this.emailEnabled)
                .discordEnabled(this.discordEnabled)
                .pushEnabled(this.pushEnabled)
                .timezone(this.timezone)
                .quietHours(this.quietHours != null ? Map.of() : null) // TODO: JSON 파싱 구현
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
