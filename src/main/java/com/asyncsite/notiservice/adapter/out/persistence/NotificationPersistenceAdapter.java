package com.asyncsite.notiservice.adapter.out.persistence;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationEntity;
import com.asyncsite.notiservice.adapter.out.persistence.repository.NotificationRepository;
import com.asyncsite.notiservice.domain.model.Notification;
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
        NotificationEntity entity = com.asyncsite.notiservice.adapter.out.persistence.mapper.NotificationEntityMapper.toEntity(notification);
        if (entity.getNotificationId() == null) {
            entity = entity.toBuilder()
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        } else {
            entity = entity.toBuilder()
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        NotificationEntity savedEntity = notificationRepository.save(entity);
        return com.asyncsite.notiservice.adapter.out.persistence.mapper.NotificationEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Notification> findNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId)
                .map(com.asyncsite.notiservice.adapter.out.persistence.mapper.NotificationEntityMapper::toDomain);
    }

    @Override
    public List<Notification> findNotificationsByUserId(String userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest)
                .getContent()
                .stream()
                .map(com.asyncsite.notiservice.adapter.out.persistence.mapper.NotificationEntityMapper::toDomain)
                .toList();
    }
}
