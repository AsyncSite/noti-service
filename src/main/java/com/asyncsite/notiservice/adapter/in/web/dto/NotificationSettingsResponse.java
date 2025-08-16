package com.asyncsite.notiservice.adapter.in.web.dto;

import com.asyncsite.notiservice.domain.model.NotificationSettings;

import java.time.LocalDateTime;

public record NotificationSettingsResponse(
        String userId,
        boolean studyUpdates,
        boolean marketing,
        boolean emailEnabled,
        boolean discordEnabled,
        boolean pushEnabled,
        String timezone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NotificationSettingsResponse from(NotificationSettings settings) {
        return new NotificationSettingsResponse(
                settings.getUserId(),
                settings.isStudyUpdates(),
                settings.isMarketing(),
                settings.isEmailEnabled(),
                settings.isDiscordEnabled(),
                settings.isPushEnabled(),
                settings.getTimezone(),
                settings.getCreatedAt(),
                settings.getUpdatedAt()
        );
    }
}
