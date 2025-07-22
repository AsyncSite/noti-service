package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.NotificationSettings;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingsUseCase {

    /**
     * 사용자의 알림 설정을 조회합니다.
     */
    Optional<NotificationSettings> getNotificationSettings(String userId);

    /**
     * 사용자의 알림 설정을 업데이트합니다.
     */
    NotificationSettings updateNotificationSettings(String userId, NotificationSettings settings);

    /**
     * 알림 설정을 초기화합니다.
     */
    NotificationSettings resetNotificationSettings(String userId);

    /**
     * 대량 사용자 설정을 조회합니다.
     */
    List<NotificationSettings> getBulkNotificationSettings(List<String> userIds, String eventType);
}
