package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.NotificationChannel;

import java.util.List;
import java.util.Optional;

public interface NotificationChannelRepositoryPort {
    List<NotificationChannel> findByNotificationId(String notificationId);
    Optional<NotificationChannel> findById(String channelId);
    NotificationChannel save(NotificationChannel channel);
} 