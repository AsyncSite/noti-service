package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.NotificationSettings;

public interface NotificationSettingsRepositoryPort {
    NotificationSettings findByUserId(String userId);
    NotificationSettings save(NotificationSettings settings);
} 