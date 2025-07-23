package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.NotificationSettings;

public interface NotificationSettingsUseCase {

    /**
     * 사용자의 알림 설정을 조회합니다.
     */
    NotificationSettings getNotificationSettings(String userId);

    /**
     * 사용자의 알림 설정을 업데이트합니다.
     */
    NotificationSettings updateNotificationSettings(String userId, boolean studyUpdates, boolean marketing, boolean emailEnabled, boolean discordEnabled, boolean pushEnabled);

    /**
     * 알림 설정을 초기화합니다.
     */
    NotificationSettings resetNotificationSettings(String userId);
}
