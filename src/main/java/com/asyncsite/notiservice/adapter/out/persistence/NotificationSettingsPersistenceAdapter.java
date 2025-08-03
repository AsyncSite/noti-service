package com.asyncsite.notiservice.adapter.out.persistence;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationSettingsEntity;
import com.asyncsite.notiservice.adapter.out.persistence.repository.NotificationSettingsRepository;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationSettingsPersistenceAdapter implements NotificationSettingsRepositoryPort {
    private final NotificationSettingsRepository settingsRepository;

    @Override
    public Optional<NotificationSettings> findByUserId(String userId) {
        return settingsRepository.findById(userId)
                .map(NotificationSettingsEntity::toDomain);
    }

    @Override
    public NotificationSettings save(NotificationSettings settings) {
        NotificationSettingsEntity entity = NotificationSettingsEntity.from(settings);
        NotificationSettingsEntity savedEntity = settingsRepository.save(entity);
        return savedEntity.toDomain();
    }
}
