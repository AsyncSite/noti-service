package com.asyncsite.notiservice.adapter.out.persistence;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationSettingsEntity;
import com.asyncsite.notiservice.adapter.out.persistence.repository.NotificationSettingsRepository;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationSettingsPersistenceAdapter implements NotificationSettingsRepositoryPort {
    private final NotificationSettingsRepository settingsRepository;

    @Override
    public NotificationSettings findByUserId(String userId) {
        return settingsRepository.findById(userId)
                .map(NotificationSettingsEntity::toDomain)
                .orElseGet(() -> NotificationSettings.builder()
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
                        .build());
    }

    @Override
    public NotificationSettings save(NotificationSettings settings) {
        NotificationSettingsEntity entity = NotificationSettingsEntity.from(settings);
        if (entity.getCreatedAt() == null) {
            entity = entity.toBuilder()
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        } else {
            entity = entity.toBuilder()
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        NotificationSettingsEntity savedEntity = settingsRepository.save(entity);
        return savedEntity.toDomain();
    }
}
