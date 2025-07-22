package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.NotificationSettings;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record NotificationSettingsResponse(
    String userId,
    boolean studyUpdates,
    boolean marketing,
    boolean emailEnabled,
    boolean discordEnabled,
    boolean pushEnabled,
    String timezone,
    String language,
    Map<String, Object> quietHours,
    Map<String, Object> channelSettings,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String message,
    List<String> changedFields,
    LocalDateTime resetAt
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
                settings.getLanguage(),
                settings.getQuietHours(),
                null, // channelSettings
                settings.getCreatedAt(),
                settings.getUpdatedAt(),
                null, // message
                null, // changedFields
                null  // resetAt
        );
    }

    public static NotificationSettingsResponse from(NotificationSettings settings, String message) {
        return new NotificationSettingsResponse(
                settings.getUserId(),
                settings.isStudyUpdates(),
                settings.isMarketing(),
                settings.isEmailEnabled(),
                settings.isDiscordEnabled(),
                settings.isPushEnabled(),
                settings.getTimezone(),
                settings.getLanguage(),
                settings.getQuietHours(),
                null, // channelSettings
                settings.getCreatedAt(),
                settings.getUpdatedAt(),
                message,
                null, // changedFields
                null  // resetAt
        );
    }

    public static NotificationSettingsResponse from(NotificationSettings settings, String message, List<String> changedFields) {
        return new NotificationSettingsResponse(
                settings.getUserId(),
                settings.isStudyUpdates(),
                settings.isMarketing(),
                settings.isEmailEnabled(),
                settings.isDiscordEnabled(),
                settings.isPushEnabled(),
                settings.getTimezone(),
                settings.getLanguage(),
                settings.getQuietHours(),
                null, // channelSettings
                settings.getCreatedAt(),
                settings.getUpdatedAt(),
                message,
                changedFields,
                null  // resetAt
        );
    }

    public static NotificationSettingsResponse from(NotificationSettings settings, String message, LocalDateTime resetAt) {
        return new NotificationSettingsResponse(
                settings.getUserId(),
                settings.isStudyUpdates(),
                settings.isMarketing(),
                settings.isEmailEnabled(),
                settings.isDiscordEnabled(),
                settings.isPushEnabled(),
                settings.getTimezone(),
                settings.getLanguage(),
                settings.getQuietHours(),
                null, // channelSettings
                settings.getCreatedAt(),
                settings.getUpdatedAt(),
                message,
                null, // changedFields
                resetAt
        );
    }
}
