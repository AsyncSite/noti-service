package com.asyncsite.notiservice.adapter.in.dto;

public record UpdateNotificationSettingsRequest(
        boolean studyUpdates,
        boolean marketing,
        boolean emailEnabled,
        boolean discordEnabled,
        boolean pushEnabled
) {
}
