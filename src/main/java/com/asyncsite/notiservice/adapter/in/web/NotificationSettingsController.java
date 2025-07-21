package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.dto.NotificationSettingsRequest;
import com.asyncsite.notiservice.adapter.in.dto.NotificationSettingsResponse;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.in.NotificationSettingsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final NotificationSettingsUseCase settingsUseCase;

    @GetMapping("/{userId}/notification-settings")
    public ResponseEntity<NotificationSettingsResponse> getNotificationSettings(@PathVariable String userId) {
        log.info("알림 설정 조회: userId={}", userId);

        return settingsUseCase.getNotificationSettings(userId)
                .map(settings -> {
                    NotificationSettingsResponse response = NotificationSettingsResponse.from(settings);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}/notification-settings")
    public ResponseEntity<NotificationSettingsResponse> updateNotificationSettings(
            @PathVariable String userId,
            @RequestBody NotificationSettingsRequest request) {

        log.info("알림 설정 업데이트: userId={}", userId);

        NotificationSettings settings = NotificationSettings.builder()
                .userId(userId)
                .studyUpdates(request.isStudyUpdates())
                .marketing(request.isMarketing())
                .emailEnabled(request.isEmailEnabled())
                .discordEnabled(request.isDiscordEnabled())
                .pushEnabled(request.isPushEnabled())
                .timezone(request.getTimezone())
                .language(request.getLanguage())
                .quietHours(request.getQuietHours())
                .build();

        NotificationSettings updatedSettings = settingsUseCase.updateNotificationSettings(userId, settings);
        NotificationSettingsResponse response = NotificationSettingsResponse.from(updatedSettings, "Notification settings updated successfully");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/notification-settings/events/{eventType}")
    public ResponseEntity<NotificationSettingsResponse> toggleEventNotification(
            @PathVariable String userId,
            @PathVariable String eventType,
            @RequestBody NotificationSettingsRequest request) {

        log.info("이벤트 알림 설정 토글: userId={}, eventType={}, enabled={}, channels={}",
                userId, eventType, request.isEnabled(), request.getChannels());

        NotificationSettings updatedSettings = settingsUseCase.toggleEventNotification(
                userId, eventType, request.isEnabled(), request.getChannels());

        NotificationSettingsResponse response = NotificationSettingsResponse.from(updatedSettings, "Event notification settings updated successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/notification-settings/reset")
    public ResponseEntity<NotificationSettingsResponse> resetNotificationSettings(@PathVariable String userId) {
        log.info("알림 설정 초기화: userId={}", userId);

        NotificationSettings resetSettings = settingsUseCase.resetNotificationSettings(userId);
        NotificationSettingsResponse response = NotificationSettingsResponse.from(
                resetSettings,
                "Notification settings reset to default values",
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/notification-settings/bulk")
    public ResponseEntity<List<NotificationSettingsResponse>> getBulkNotificationSettings(
            @RequestParam List<String> userIds,
            @RequestParam(required = false) String eventType) {

        log.info("대량 알림 설정 조회: userIds={}, eventType={}", userIds, eventType);

        List<NotificationSettings> settingsList = settingsUseCase.getBulkNotificationSettings(userIds, eventType);
        List<NotificationSettingsResponse> responses = settingsList.stream()
                .map(NotificationSettingsResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
