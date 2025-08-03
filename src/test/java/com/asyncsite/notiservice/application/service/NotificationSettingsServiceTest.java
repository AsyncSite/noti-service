package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationSettingsService 테스트")
class NotificationSettingsServiceTest {

    @Mock
    private NotificationSettingsRepositoryPort settingsRepository;

    @InjectMocks
    private NotificationSettingsService settingsService;

    @Test
    @DisplayName("기존 알림 설정을 조회할 수 있다")
    void getExistingNotificationSettings() {
        // given
        String userId = "user123";
        NotificationSettings existingSettings = NotificationSettings.createDefault(userId);
        
        when(settingsRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingSettings));

        // when
        NotificationSettings result = settingsService.getNotificationSettings(userId);

        // then
        assertThat(result).isEqualTo(existingSettings);
        verify(settingsRepository).findByUserId(userId);
        verify(settingsRepository, never()).save(any());
    }

    @Test
    @DisplayName("알림 설정이 없으면 기본 설정을 생성하여 반환한다")
    void getNotificationSettings_CreateDefault() {
        // given
        String userId = "user123";
        NotificationSettings defaultSettings = NotificationSettings.createDefault(userId);
        
        when(settingsRepository.findByUserId(userId))
                .thenReturn(Optional.empty());
        when(settingsRepository.save(any(NotificationSettings.class)))
                .thenReturn(defaultSettings);

        // when
        NotificationSettings result = settingsService.getNotificationSettings(userId);

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.isStudyUpdates()).isTrue();
        assertThat(result.isEmailEnabled()).isTrue();
        
        verify(settingsRepository).findByUserId(userId);
        verify(settingsRepository).save(any(NotificationSettings.class));
    }

    @Test
    @DisplayName("알림 설정을 업데이트할 수 있다")
    void updateNotificationSettings() {
        // given
        String userId = "user123";
        NotificationSettings existingSettings = NotificationSettings.createDefault(userId);
        NotificationSettings updatedSettings = existingSettings.toBuilder()
                .studyUpdates(false)
                .marketing(false)
                .emailEnabled(true)
                .discordEnabled(false)
                .pushEnabled(true)
                .build();

        when(settingsRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingSettings));
        when(settingsRepository.save(any(NotificationSettings.class)))
                .thenReturn(updatedSettings);

        // when
        NotificationSettings result = settingsService.updateNotificationSettings(
                userId, false, false, true, false, true
        );

        // then
        assertThat(result.isStudyUpdates()).isFalse();
        assertThat(result.isMarketing()).isFalse();
        assertThat(result.isEmailEnabled()).isTrue();
        assertThat(result.isDiscordEnabled()).isFalse();
        assertThat(result.isPushEnabled()).isTrue();
        
        verify(settingsRepository).findByUserId(userId);
        verify(settingsRepository).save(any(NotificationSettings.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 설정 업데이트시 예외가 발생한다")
    void updateNotificationSettings_UserNotFound() {
        // given
        String userId = "nonexistent";
        
        when(settingsRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {
            settingsService.updateNotificationSettings(userId, false, false, true, false, true);
        }).isInstanceOf(RuntimeException.class);
        
        verify(settingsRepository).findByUserId(userId);
        verify(settingsRepository, never()).save(any());
    }

    @Test
    @DisplayName("알림 설정을 초기화할 수 있다")
    void resetNotificationSettings() {
        // given
        String userId = "user123";
        NotificationSettings existingSettings = NotificationSettings.createDefault(userId);
        existingSettings.update(false, false, false, false, false); // 모든 설정 비활성화
        
        NotificationSettings resetSettings = existingSettings.reset();

        when(settingsRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingSettings));
        when(settingsRepository.save(any(NotificationSettings.class)))
                .thenReturn(resetSettings);

        // when
        NotificationSettings result = settingsService.resetNotificationSettings(userId);

        // then
        assertThat(result.isStudyUpdates()).isTrue();
        assertThat(result.isMarketing()).isFalse(); // 초기화시 마케팅은 false
        assertThat(result.isEmailEnabled()).isTrue();
        assertThat(result.isDiscordEnabled()).isFalse(); // 초기화시 디스코드는 false
        assertThat(result.isPushEnabled()).isFalse(); // 초기화시 푸시는 false
        
        verify(settingsRepository).findByUserId(userId);
        verify(settingsRepository).save(any(NotificationSettings.class));
    }
} 