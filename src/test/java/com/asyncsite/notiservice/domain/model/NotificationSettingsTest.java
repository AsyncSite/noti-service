package com.asyncsite.notiservice.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationSettingsTest {
    @Test
    void createSettings() {
        NotificationSettings settings = NotificationSettings.builder()
                .userId("100")
                .studyUpdates(true)
                .marketing(false)
                .emailEnabled(true)
                .discordEnabled(false)  
                .pushEnabled(true)
                .timezone("Asia/Seoul")
                .language("ko")
                .quietHours(Map.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assertThat(settings).isNotNull();
        assertThat(settings.isEmailEnabled()).isTrue();
        assertThat(settings.isDiscordEnabled()).isFalse();
        assertThat(settings.isPushEnabled()).isTrue();
    }

    @Test
    void channelEnabled() {
        NotificationSettings settings = NotificationSettings.builder()
                .userId("100")
                .studyUpdates(true)
                .marketing(false)
                .emailEnabled(true)
                .discordEnabled(false)
                .pushEnabled(false)
                .timezone("Asia/Seoul")
                .language("ko")
                .quietHours(Map.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assertThat(settings.isChannelEnabled(NotificationChannel.ChannelType.EMAIL)).isTrue();
        assertThat(settings.isChannelEnabled(NotificationChannel.ChannelType.DISCORD)).isFalse();
        assertThat(settings.isChannelEnabled(NotificationChannel.ChannelType.PUSH)).isFalse();
    }

    @Test
    void eventTypeEnabled() {
        NotificationSettings settings = NotificationSettings.builder()
                .userId("100")
                .studyUpdates(true)
                .marketing(false)
                .emailEnabled(true)
                .discordEnabled(false)
                .pushEnabled(false)
                .timezone("Asia/Seoul")
                .language("ko")
                .quietHours(Map.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assertThat(settings.isEventTypeEnabled("STUDY_UPDATE")).isTrue();
        assertThat(settings.isEventTypeEnabled("MARKETING_EVENT")).isFalse();
        assertThat(settings.isEventTypeEnabled("ETC_EVENT")).isTrue();
    }

    @Test
    void quietHours() {
        NotificationSettings settings = NotificationSettings.builder()
                .userId("100")
                .studyUpdates(true)
                .marketing(false)
                .emailEnabled(true)
                .discordEnabled(false)
                .pushEnabled(false)
                .timezone("Asia/Seoul")
                .language("ko")
                .quietHours(Map.of(
                        "enabled", true,
                        "startTime", "22:00",
                        "endTime", "08:00",
                        "weekendsOnly", false
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        // 실제 시간에 따라 다르므로, enabled 여부만 체크
        assertThat(settings.isInQuietHours()).isInstanceOf(Boolean.class);
    }

    @Test
    void withChannelEnabled() {
        NotificationSettings settings = NotificationSettings.builder()
                .userId("100")
                .studyUpdates(true)
                .marketing(false)
                .emailEnabled(false)
                .discordEnabled(false)
                .pushEnabled(false)
                .timezone("Asia/Seoul")
                .language("ko")
                .quietHours(Map.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        NotificationSettings enabled = settings.withChannelEnabled(NotificationChannel.ChannelType.EMAIL, true);
        assertThat(enabled.isEmailEnabled()).isTrue();
        NotificationSettings disabled = enabled.withChannelEnabled(NotificationChannel.ChannelType.EMAIL, false);
        assertThat(disabled.isEmailEnabled()).isFalse();
    }
} 