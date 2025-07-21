package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.in.NotificationSettingsUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingsService implements NotificationSettingsUseCase {

    private final NotificationSettingsRepositoryPort settingsRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationSettings> getNotificationSettings(String userId) {
        log.info("알림 설정 조회: userId={}", userId);
        return Optional.of(settingsRepository.findByUserId(userId));
    }

    @Override
    public NotificationSettings updateNotificationSettings(String userId, NotificationSettings settings) {
        log.info("알림 설정 업데이트: userId={}", userId);

        NotificationSettings updatedSettings = settings.toBuilder()
                .userId(userId)
                .updatedAt(LocalDateTime.now())
                .build();

        return settingsRepository.save(updatedSettings);
    }

    @Override
    public NotificationSettings toggleEventNotification(String userId, String eventType, boolean enabled, List<String> channels) {
        log.info("이벤트 알림 설정 토글: userId={}, eventType={}, enabled={}, channels={}",
                userId, eventType, enabled, channels);

        NotificationSettings currentSettings = settingsRepository.findByUserId(userId);

        // 이벤트 타입에 따른 설정 업데이트
        NotificationSettings updatedSettings = currentSettings;

        if (eventType.startsWith("STUDY_")) {
            updatedSettings = updatedSettings.toBuilder()
                    .studyUpdates(enabled)
                    .updatedAt(LocalDateTime.now())
                    .build();
        } else if (eventType.startsWith("MARKETING_")) {
            updatedSettings = updatedSettings.toBuilder()
                    .marketing(enabled)
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        // 채널별 설정 업데이트
        if (channels.contains("EMAIL")) {
            updatedSettings = updatedSettings.withChannelEnabled(
                    com.asyncsite.notiservice.domain.model.NotificationChannel.ChannelType.EMAIL, enabled);
        }
        if (channels.contains("DISCORD")) {
            updatedSettings = updatedSettings.withChannelEnabled(
                    com.asyncsite.notiservice.domain.model.NotificationChannel.ChannelType.DISCORD, enabled);
        }
        if (channels.contains("PUSH")) {
            updatedSettings = updatedSettings.withChannelEnabled(
                    com.asyncsite.notiservice.domain.model.NotificationChannel.ChannelType.PUSH, enabled);
        }

        return settingsRepository.save(updatedSettings);
    }

    @Override
    public NotificationSettings resetNotificationSettings(String userId) {
        log.info("알림 설정 초기화: userId={}", userId);

        NotificationSettings defaultSettings = NotificationSettings.builder()
                .userId(userId)
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

        return settingsRepository.save(defaultSettings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSettings> getBulkNotificationSettings(List<String> userIds, String eventType) {
        log.info("대량 알림 설정 조회: userIds={}, eventType={}", userIds, eventType);

        return userIds.stream()
                .map(userId -> settingsRepository.findByUserId(userId))
                .filter(settings -> {
                    if (eventType == null) return true;
                    return settings.isEventTypeEnabled(eventType);
                })
                .toList();
    }
}
