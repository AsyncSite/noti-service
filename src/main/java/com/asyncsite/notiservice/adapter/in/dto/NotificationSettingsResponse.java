package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.NotificationSettings;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class NotificationSettingsResponse {

    private final String userId;
    private final boolean studyUpdates;
    private final boolean marketing;
    private final boolean emailEnabled;
    private final boolean discordEnabled;
    private final boolean pushEnabled;
    private final String timezone;
    private final String language;
    private final Map<String, Object> quietHours;
    private final Map<String, Object> channelSettings;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String message;
    private final List<String> changedFields;
    private final LocalDateTime resetAt;

    public static NotificationSettingsResponse from(NotificationSettings settings) {
        return NotificationSettingsResponse.builder()
                .userId(settings.getUserId())
                .studyUpdates(settings.isStudyUpdates())
                .marketing(settings.isMarketing())
                .emailEnabled(settings.isEmailEnabled())
                .discordEnabled(settings.isDiscordEnabled())
                .pushEnabled(settings.isPushEnabled())
                .timezone(settings.getTimezone())
                .language(settings.getLanguage())
                .quietHours(settings.getQuietHours())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    public static NotificationSettingsResponse from(NotificationSettings settings, String message) {
        return NotificationSettingsResponse.builder()
                .userId(settings.getUserId())
                .studyUpdates(settings.isStudyUpdates())
                .marketing(settings.isMarketing())
                .emailEnabled(settings.isEmailEnabled())
                .discordEnabled(settings.isDiscordEnabled())
                .pushEnabled(settings.isPushEnabled())
                .timezone(settings.getTimezone())
                .language(settings.getLanguage())
                .quietHours(settings.getQuietHours())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .message(message)
                .build();
    }

    public static NotificationSettingsResponse from(NotificationSettings settings, String message, List<String> changedFields) {
        return NotificationSettingsResponse.builder()
                .userId(settings.getUserId())
                .studyUpdates(settings.isStudyUpdates())
                .marketing(settings.isMarketing())
                .emailEnabled(settings.isEmailEnabled())
                .discordEnabled(settings.isDiscordEnabled())
                .pushEnabled(settings.isPushEnabled())
                .timezone(settings.getTimezone())
                .language(settings.getLanguage())
                .quietHours(settings.getQuietHours())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .message(message)
                .changedFields(changedFields)
                .build();
    }

    public static NotificationSettingsResponse from(NotificationSettings settings, String message, LocalDateTime resetAt) {
        return NotificationSettingsResponse.builder()
                .userId(settings.getUserId())
                .studyUpdates(settings.isStudyUpdates())
                .marketing(settings.isMarketing())
                .emailEnabled(settings.isEmailEnabled())
                .discordEnabled(settings.isDiscordEnabled())
                .pushEnabled(settings.isPushEnabled())
                .timezone(settings.getTimezone())
                .language(settings.getLanguage())
                .quietHours(settings.getQuietHours())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .message(message)
                .resetAt(resetAt)
                .build();
    }
}
