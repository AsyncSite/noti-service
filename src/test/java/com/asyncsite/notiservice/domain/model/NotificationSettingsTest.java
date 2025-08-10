package com.asyncsite.notiservice.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationSettings 도메인 모델 테스트")
class NotificationSettingsTest {

    @Test
    @DisplayName("기본 알림 설정을 생성할 수 있다")
    void createDefaultSettings() {
        // given
        String userId = "user123";

        // when
        NotificationSettings settings = NotificationSettings.createDefault(userId);

        // then
        assertThat(settings.getUserId()).isEqualTo(userId);
        assertThat(settings.isStudyUpdates()).isTrue();
        assertThat(settings.isMarketing()).isTrue();
        assertThat(settings.isEmailEnabled()).isTrue();
        assertThat(settings.isDiscordEnabled()).isTrue();
        assertThat(settings.isPushEnabled()).isTrue();
        assertThat(settings.getTimezone()).isEqualTo("Asia/Seoul");
        assertThat(settings.getCreatedAt()).isNotNull();
        assertThat(settings.getUpdatedAt()).isNotNull(); // 기본 생성시에는 null
    }

    @Test
    @DisplayName("알림 설정을 업데이트할 수 있다")
    void updateSettings() {
        // given
        NotificationSettings settings = NotificationSettings.createDefault("user123");
        boolean studyUpdates = false;
        boolean marketing = false;
        boolean emailEnabled = true;
        boolean discordEnabled = false;
        boolean pushEnabled = true;

        // when
        settings.update(studyUpdates, marketing, emailEnabled, discordEnabled, pushEnabled);

        // then
        assertThat(settings.isStudyUpdates()).isEqualTo(studyUpdates);
        assertThat(settings.isMarketing()).isEqualTo(marketing);
        assertThat(settings.isEmailEnabled()).isEqualTo(emailEnabled);
        assertThat(settings.isDiscordEnabled()).isEqualTo(discordEnabled);
        assertThat(settings.isPushEnabled()).isEqualTo(pushEnabled);
        assertThat(settings.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("알림 설정을 초기화할 수 있다")
    void resetSettings() {
        // given
        NotificationSettings settings = NotificationSettings.createDefault("user123");
        settings.update(false, false, false, false, false); // 모든 설정 비활성화

        // when
        NotificationSettings resetSettings = settings.reset();

        // then
        assertThat(resetSettings.getUserId()).isEqualTo("user123");
        assertThat(resetSettings.isStudyUpdates()).isTrue();
        assertThat(resetSettings.isMarketing()).isFalse(); // 초기화시 마케팅은 false
        assertThat(resetSettings.isEmailEnabled()).isTrue();
        assertThat(resetSettings.isDiscordEnabled()).isFalse(); // 초기화시 디스코드는 false
        assertThat(resetSettings.isPushEnabled()).isFalse(); // 초기화시 푸시는 false
        assertThat(resetSettings.getTimezone()).isEqualTo("Asia/Seoul");
        assertThat(resetSettings.getCreatedAt()).isEqualTo(settings.getCreatedAt()); // 생성시간 유지
        assertThat(resetSettings.getUpdatedAt()).isNotNull(); // 업데이트 시간 갱신
    }
}
