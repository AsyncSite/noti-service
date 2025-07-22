package com.asyncsite.notiservice.adapter.out.persistence;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationEntity;
import com.asyncsite.notiservice.adapter.out.persistence.repository.NotificationRepository;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.port.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationRepositoryPort {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification saveNotification(Notification notification) {
        NotificationEntity entity = NotificationEntity.from(notification);
        NotificationEntity savedEntity = notificationRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Notification> findNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId)
                .map(NotificationEntity::toDomain);
    }

    @Override
    public List<Notification> findNotificationsByUserId(String userId, ChannelType channelType, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return notificationRepository.findByUserIdAndChannelTypeOrderByCreatedAtDesc(userId, channelType, pageRequest)
                .getContent()
                .stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }
}
