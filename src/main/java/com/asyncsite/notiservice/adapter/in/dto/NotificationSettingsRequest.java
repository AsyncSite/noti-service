package com.asyncsite.notiservice.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record NotificationSettingsRequest(
    boolean studyUpdates,
    boolean marketing,
    boolean emailEnabled,
    boolean discordEnabled,
    boolean pushEnabled,
    @NotBlank(message = "시간대는 필수입니다.")
    String timezone,
    @NotBlank(message = "언어는 필수입니다.")
    String language,
    Map<String, Object> quietHours,
    Map<String, Object> channelSettings,
    boolean enabled,
    List<String> channels
) {
    // 기본값을 제공하는 생성자
    public NotificationSettingsRequest {
        // 기본값 설정
        if (timezone == null || timezone.isBlank()) {
            timezone = "Asia/Seoul";
        }
        if (language == null || language.isBlank()) {
            language = "ko";
        }
    }
} 