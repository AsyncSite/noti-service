package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class NotificationSettingsServiceTest {
    @Mock
    private NotificationSettingsRepositoryPort settingsRepository;

    @InjectMocks
    private NotificationSettingsService settingsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getNotificationSettings() {
        NotificationSettings settings = NotificationSettings.builder()
                .userId("100")
                .studyUpdates(true)
                .marketing(false)
                .emailEnabled(true)
                .discordEnabled(false)
                .pushEnabled(false)
                .timezone("Asia/Seoul")
                .language("ko")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        given(settingsRepository.findByUserId("100")).willReturn(settings);
        Optional<NotificationSettings> result = settingsService.getNotificationSettings("100");
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo("100");
    }

    @Test
    void updateNotificationSettings() {
        NotificationSettings settings = NotificationSettings.builder()
                .userId("100")
                .studyUpdates(true)
                .marketing(false)
                .emailEnabled(true)
                .discordEnabled(false)
                .pushEnabled(false)
                .timezone("Asia/Seoul")
                .language("ko")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        given(settingsRepository.save(ArgumentMatchers.any())).willReturn(settings);
        NotificationSettings result = settingsService.updateNotificationSettings("100", settings);
        assertThat(result.getUserId()).isEqualTo("100");
    }

    @Test
    void toggleEventNotification() {
        NotificationSettings settings = NotificationSettings.builder()
                .userId("100")
                .studyUpdates(false)
                .marketing(false)
                .emailEnabled(false)
                .discordEnabled(false)
                .pushEnabled(false)
                .timezone("Asia/Seoul")
                .language("ko")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        given(settingsRepository.findByUserId("100")).willReturn(settings);
        given(settingsRepository.save(ArgumentMatchers.any())).willAnswer(invocation -> invocation.getArgument(0));
        NotificationSettings result = settingsService.toggleEventNotification("100", "STUDY_UPDATE", true, List.of("EMAIL"));
        assertThat(result.isStudyUpdates()).isTrue();
        assertThat(result.isEmailEnabled()).isTrue();
    }

    @Test
    void resetNotificationSettings() {
        given(settingsRepository.save(ArgumentMatchers.any())).willAnswer(invocation -> invocation.getArgument(0));
        NotificationSettings result = settingsService.resetNotificationSettings("100");
        assertThat(result.isStudyUpdates()).isTrue();
        assertThat(result.isMarketing()).isFalse();
        assertThat(result.isEmailEnabled()).isTrue();
    }

    @Test
    void getBulkNotificationSettings() {
        NotificationSettings s1 = NotificationSettings.builder().userId("1").studyUpdates(true).marketing(false).emailEnabled(true).discordEnabled(false).pushEnabled(false).timezone("Asia/Seoul").language("ko").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        NotificationSettings s2 = NotificationSettings.builder().userId("2").studyUpdates(false).marketing(false).emailEnabled(false).discordEnabled(false).pushEnabled(false).timezone("Asia/Seoul").language("ko").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        given(settingsRepository.findByUserId("1")).willReturn(s1);
        given(settingsRepository.findByUserId("2")).willReturn(s2);
        List<NotificationSettings> result = settingsService.getBulkNotificationSettings(List.of("1", "2"), null);
        assertThat(result).hasSize(2);
    }
}
