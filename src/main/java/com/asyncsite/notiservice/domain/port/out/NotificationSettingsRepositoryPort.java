package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.NotificationSettings;

import java.util.Optional;

public interface NotificationSettingsRepositoryPort {
    Optional<NotificationSettings> findByUserId(String userId);
    NotificationSettings save(NotificationSettings settings);
}
