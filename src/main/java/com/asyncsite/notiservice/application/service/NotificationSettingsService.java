package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.in.NotificationSettingsUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return settingsRepository.findByUserId(userId);
    }

    @Override
    public NotificationSettings updateNotificationSettings(String userId, NotificationSettings settings) {
        log.info("알림 설정 업데이트: userId={}", userId);

        // 기존 설정을 조회하고 도메인 행위 메서드를 사용하여 업데이트
        Optional<NotificationSettings> existingSettings = settingsRepository.findByUserId(userId);

        // 도메인 행위 메서드를 통한 업데이트
        NotificationSettings updatedSettings = existingSettings.get()
                .updateEventSettings(settings.isStudyUpdates(), settings.isMarketing());

        // 채널 설정 업데이트
        updatedSettings = updatedSettings
                .updateChannelEnabled(NotificationChannel.ChannelType.EMAIL, settings.isEmailEnabled())
                .updateChannelEnabled(NotificationChannel.ChannelType.DISCORD, settings.isDiscordEnabled())
                .updateChannelEnabled(NotificationChannel.ChannelType.PUSH, settings.isPushEnabled());

        // 지역화 설정 업데이트
        if (!settings.getTimezone().equals(existingSettings.getTimezone()) ||
            !Objects.equals(settings.getLanguage(), existingSettings.getLanguage())) {

            updatedSettings = updatedSettings.updateLocalizationSettings(
                    settings.getTimezone(),
                    settings.getLanguage()
            );
        }

        // 방해금지 시간 업데이트
        if (!Objects.equals(settings.getQuietHours(), existingSettings.getQuietHours())) {
            updatedSettings = updatedSettings.updateQuietHours(settings.getQuietHours());
        }

        return settingsRepository.save(updatedSettings);
    }

    @Override
    public NotificationSettings toggleEventNotification(String userId, String eventType, boolean enabled, List<String> channels) {
        log.info("이벤트 알림 설정 토글: userId={}, eventType={}, enabled={}, channels={}",
                userId, eventType, enabled, channels);

        NotificationSettings currentSettings = settingsRepository.findByUserId(userId);
        NotificationSettings updatedSettings = currentSettings;

        // 이벤트 타입에 따른 설정 업데이트 (도메인 행위 메서드 사용)
        if (eventType.startsWith("STUDY_")) {
            updatedSettings = updatedSettings.updateEventSettings(enabled, currentSettings.isMarketing());
        } else if (eventType.startsWith("MARKETING_")) {
            updatedSettings = updatedSettings.updateEventSettings(currentSettings.isStudyUpdates(), enabled);
        }

        // 채널별 설정 업데이트 (도메인 메서드 사용)
        if (channels != null) {
            for (String channelStr : channels) {
                try {
                    NotificationChannel.ChannelType channelType = NotificationChannel.ChannelType.valueOf(channelStr);
                    updatedSettings = updatedSettings.updateChannelEnabled(channelType, enabled);
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 채널 타입: {}", channelStr);
                }
            }
        }

        return settingsRepository.save(updatedSettings);
    }

    @Override
    public NotificationSettings resetNotificationSettings(String userId) {
        log.info("알림 설정 초기화: userId={}", userId);

        // 도메인 팩토리 메서드 사용
        NotificationSettings defaultSettings = NotificationSettings.createDefault(userId);

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
