package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.in.NotificationSettingsUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingsService implements NotificationSettingsUseCase {

    private final NotificationSettingsRepositoryPort settingsRepository;

    @Override
    public NotificationSettings getNotificationSettings(String userId) {
        log.info("알림 설정 조회: userId={}", userId);
        Optional<NotificationSettings> settings = settingsRepository.findByUserId(userId);
        return settings.orElseGet(() -> settingsRepository.save(NotificationSettings.createDefault(userId)));
    }

    @Override
    public NotificationSettings updateNotificationSettings(String userId, boolean studyUpdates, boolean marketing, boolean emailEnabled, boolean discordEnabled, boolean pushEnabled) {
        log.info("알림 설정 업데이트: userId={}", userId);
        // 기존 설정을 조회하고 도메인 행위 메서드를 사용하여 업데이트
        NotificationSettings updateSetting = settingsRepository.findByUserId(userId).orElseThrow();
        updateSetting.update(studyUpdates, marketing, emailEnabled, discordEnabled, pushEnabled);
        return settingsRepository.save(updateSetting);
    }

    @Override
    public NotificationSettings resetNotificationSettings(String userId) {
        log.info("알림 설정 초기화: userId={}", userId);
        // 도메인 팩토리 메서드 사용
        NotificationSettings setting = getNotificationSettings(userId);
        setting.reset();
        return settingsRepository.save(setting);
    }
}
