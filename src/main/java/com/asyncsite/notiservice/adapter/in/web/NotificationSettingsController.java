package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.coreplatform.common.dto.ApiResponse;
import com.asyncsite.notiservice.adapter.in.web.dto.NotificationSettingsResponse;
import com.asyncsite.notiservice.adapter.in.web.dto.UpdateNotificationSettingsRequest;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.in.NotificationSettingsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/noti/settings")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final NotificationSettingsUseCase settingsUseCase;

    @GetMapping("/{userId}")
    public ApiResponse<NotificationSettingsResponse> getNotificationSettings(@PathVariable String userId) {
        log.info("알림 설정 조회: userId={}", userId);

        return ApiResponse.success(NotificationSettingsResponse.from( settingsUseCase.getNotificationSettings(userId)));
    }

    @PutMapping("/{userId}")
    public ApiResponse<NotificationSettingsResponse> updateNotificationSettings(
            @PathVariable String userId,
            @Valid @RequestBody UpdateNotificationSettingsRequest request) {

        log.info("알림 설정 업데이트: userId={}", userId);
        return ApiResponse.success(NotificationSettingsResponse.from(settingsUseCase.updateNotificationSettings(
                userId,
                request.studyUpdates(),
                request.marketing(),
                request.emailEnabled(),
                request.discordEnabled(),
                request.pushEnabled()
        )));
    }

    @PostMapping("/{userId}/reset")
    public ApiResponse<NotificationSettingsResponse> resetNotificationSettings(@PathVariable String userId) {
        log.info("알림 설정 초기화: userId={}", userId);

        NotificationSettings resetSettings = settingsUseCase.resetNotificationSettings(userId);
        NotificationSettingsResponse response = NotificationSettingsResponse.from(resetSettings);

        return ApiResponse.success(response);
    }
}
