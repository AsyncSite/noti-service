package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.dto.NotificationSettingsRequest;
import com.asyncsite.notiservice.adapter.in.dto.NotificationSettingsResponse;
import com.asyncsite.notiservice.adapter.in.mapper.NotificationMapper;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.in.NotificationSettingsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @Valid @RequestBody NotificationSettingsRequest request) {

        log.info("알림 설정 업데이트: userId={}", userId);

        // Mapper를 사용하여 Request에서 Domain 객체로 변환
        NotificationSettings settings = NotificationMapper.toNotificationSettings(userId, request);
        
        NotificationSettings updatedSettings = settingsUseCase.updateNotificationSettings(userId, settings);
        NotificationSettingsResponse response = NotificationSettingsResponse.from(updatedSettings, "Notification settings updated successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/notification-settings/reset")
    public ResponseEntity<NotificationSettingsResponse> resetNotificationSettings(@PathVariable String userId) {
        log.info("알림 설정 초기화: userId={}", userId);

        NotificationSettings resetSettings = settingsUseCase.resetNotificationSettings(userId);
        NotificationSettingsResponse response = NotificationSettingsResponse.from(resetSettings, "Notification settings have been reset to default");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/notification-settings/bulk")
    public ResponseEntity<List<NotificationSettingsResponse>> getBulkNotificationSettings(
            @RequestBody List<String> userIds,
            @RequestParam(required = false) String eventType) {

        log.info("대량 알림 설정 조회: userIds={}, eventType={}", userIds, eventType);

        List<NotificationSettings> settingsList = settingsUseCase.getBulkNotificationSettings(userIds, eventType);
        List<NotificationSettingsResponse> responses = settingsList.stream()
                .map(NotificationSettingsResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
