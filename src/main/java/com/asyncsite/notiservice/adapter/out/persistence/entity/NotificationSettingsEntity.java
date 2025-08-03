package com.asyncsite.notiservice.adapter.out.persistence.entity;

import com.asyncsite.notiservice.domain.model.NotificationSettings;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    @Version
    private Long version;
    private boolean studyUpdates;
    private boolean marketing;
    private boolean emailEnabled;
    private boolean discordEnabled;
    private boolean pushEnabled;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static NotificationSettingsEntity from(NotificationSettings settings) {
        return NotificationSettingsEntity.builder()
                .userId(settings.getUserId())
                .version(settings.getVersion())
                .studyUpdates(settings.isStudyUpdates())
                .marketing(settings.isMarketing())
                .emailEnabled(settings.isEmailEnabled())
                .discordEnabled(settings.isDiscordEnabled())
                .pushEnabled(settings.isPushEnabled())
                .timezone(settings.getTimezone())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    public NotificationSettings toDomain() {
        return NotificationSettings.builder()
                .userId(userId)
                .version(version)
                .studyUpdates(studyUpdates)
                .marketing(marketing)
                .emailEnabled(emailEnabled)
                .discordEnabled(discordEnabled)
                .pushEnabled(pushEnabled)
                .timezone(timezone)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
