package com.asyncsite.notiservice.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder(toBuilder = true)
public class NotificationSettings {
    private String userId;
    private boolean studyUpdates;
    private boolean marketing;
    private boolean emailEnabled;
    private boolean discordEnabled;
    private boolean pushEnabled;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    /**
     * 기본 알림 설정을 생성합니다.
     */
    public static NotificationSettings createDefault(String userId) {
        LocalDateTime now = LocalDateTime.now();
        return NotificationSettings.builder()
                .userId(userId)
                .studyUpdates(true)
                .marketing(false)
                .emailEnabled(true)
                .discordEnabled(false)
                .pushEnabled(false)
                .timezone("Asia/Seoul")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 커스텀 알림 설정을 생성합니다.
     */
    public static NotificationSettings create(
            String userId,
            boolean studyUpdates,
            boolean marketing,
            boolean emailEnabled,
            boolean discordEnabled,
            boolean pushEnabled,
            String timezone) {

        LocalDateTime now = LocalDateTime.now();
        return NotificationSettings.builder()
                .userId(userId)
                .studyUpdates(studyUpdates)
                .marketing(marketing)
                .emailEnabled(emailEnabled)
                .discordEnabled(discordEnabled)
                .pushEnabled(pushEnabled)
                .timezone(timezone != null ? timezone : "Asia/Seoul")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 설정을 초기화합니다.
     */
    public NotificationSettings reset() {
        return NotificationSettings.builder()
                .userId(this.userId)
                .studyUpdates(true)
                .marketing(false)
                .emailEnabled(true)
                .discordEnabled(false)
                .pushEnabled(false)
                .timezone("Asia/Seoul")
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
