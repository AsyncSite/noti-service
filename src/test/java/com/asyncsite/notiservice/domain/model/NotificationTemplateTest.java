package com.asyncsite.notiservice.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTemplateTest {
    @Test
    void createTemplate() {
        NotificationTemplate template = NotificationTemplate.builder()
                .templateId("1L")
                .eventType("STUDY_UPDATE")
                .channelType(NotificationChannel.ChannelType.EMAIL)
                .language("ko")
                .titleTemplate("[{{user}}] 스터디 알림")
                .contentTemplate("{{user}}님, 스터디가 곧 시작됩니다.")
                .variables(Map.of("user", "홍길동"))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assertThat(template).isNotNull();
        assertThat(template.isActive()).isTrue();
    }

    @Test
    void renderTemplate() {
        NotificationTemplate template = NotificationTemplate.builder()
                .templateId("1L")
                .eventType("STUDY_UPDATE")
                .channelType(NotificationChannel.ChannelType.EMAIL)
                .language("ko")
                .titleTemplate("[{{user}}] 스터디 알림")
                .contentTemplate("{{user}}님, 스터디가 곧 시작됩니다.")
                .variables(Map.of("user", "홍길동"))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        String title = template.renderTitle(Map.of("user", "홍길동"));
        String content = template.renderContent(Map.of("user", "홍길동"));
        assertThat(title).isEqualTo("[홍길동] 스터디 알림");
        assertThat(content).isEqualTo("홍길동님, 스터디가 곧 시작됩니다.");
    }

    @Test
    void eventTypeChannelTypeLanguageMatch() {
        NotificationTemplate template = NotificationTemplate.builder()
                .templateId("1L")
                .eventType("STUDY_UPDATE")
                .channelType(NotificationChannel.ChannelType.EMAIL)
                .language("ko")
                .titleTemplate("")
                .contentTemplate("")
                .variables(Map.of())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assertThat(template.isForEventType("STUDY_UPDATE")).isTrue();
        assertThat(template.isForEventType("OTHER_EVENT")).isFalse();
        assertThat(template.isForChannelType(NotificationChannel.ChannelType.EMAIL)).isTrue();
        assertThat(template.isForChannelType(NotificationChannel.ChannelType.PUSH)).isFalse();
        assertThat(template.isForLanguage("ko")).isTrue();
        assertThat(template.isForLanguage("en")).isFalse();
    }
}
