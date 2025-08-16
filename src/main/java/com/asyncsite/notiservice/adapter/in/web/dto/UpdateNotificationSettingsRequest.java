package com.asyncsite.notiservice.adapter.in.web.dto;

public record UpdateNotificationSettingsRequest(
        boolean studyUpdates,
        boolean marketing,
        boolean emailEnabled,
        boolean discordEnabled,
        boolean pushEnabled
) {
}
