package com.asyncsite.notiservice.adapter.out.persistence;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationChannelEntity;
import com.asyncsite.notiservice.adapter.out.persistence.repository.NotificationChannelRepository;
import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.port.out.NotificationChannelRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationChannelPersistenceAdapter implements NotificationChannelRepositoryPort {
    private final NotificationChannelRepository channelRepository;

    @Override
    public List<NotificationChannel> findByNotificationId(String notificationId) {
        return channelRepository.findByNotificationId(notificationId)
                .stream()
                .map(NotificationChannelEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<NotificationChannel> findById(String channelId) {
        return channelRepository.findById(channelId).map(NotificationChannelEntity::toDomain);
    }

    @Override
    public NotificationChannel save(NotificationChannel channel) {
        NotificationChannelEntity entity = NotificationChannelEntity.from(channel);
        NotificationChannelEntity saved = channelRepository.save(entity);
        return saved.toDomain();
    }
}
