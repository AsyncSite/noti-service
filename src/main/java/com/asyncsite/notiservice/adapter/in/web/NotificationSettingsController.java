package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.dto.NotificationSettingsResponse;
import com.asyncsite.notiservice.adapter.in.dto.UpdateNotificationSettingsRequest;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.in.NotificationSettingsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final NotificationSettingsUseCase settingsUseCase;
    private final NotificationSettingsUseCase notificationSettingsUseCase;

    @GetMapping("/{userId}/notification-settings")
    public ResponseEntity<NotificationSettingsResponse> getNotificationSettings(@PathVariable String userId) {
        log.info("알림 설정 조회: userId={}", userId);

        return ResponseEntity.ok(NotificationSettingsResponse.from( settingsUseCase.getNotificationSettings(userId)));
    }

    @PutMapping("/{userId}/notification-settings")
    public ResponseEntity<NotificationSettingsResponse> updateNotificationSettings(
            @PathVariable String userId,
            @Valid @RequestBody UpdateNotificationSettingsRequest request) {

        log.info("알림 설정 업데이트: userId={}", userId);
        return ResponseEntity.ok(NotificationSettingsResponse.from(notificationSettingsUseCase.updateNotificationSettings(
                userId,
                request.studyUpdates(),
                request.marketing(),
                request.emailEnabled(),
                request.discordEnabled(),
                request.pushEnabled()
        )));
    }

    @PostMapping("/{userId}/notification-settings/reset")
    public ResponseEntity<NotificationSettingsResponse> resetNotificationSettings(@PathVariable String userId) {
        log.info("알림 설정 초기화: userId={}", userId);

        NotificationSettings resetSettings = settingsUseCase.resetNotificationSettings(userId);
        NotificationSettingsResponse response = NotificationSettingsResponse.from(resetSettings);

        return ResponseEntity.ok(response);
    }
}
