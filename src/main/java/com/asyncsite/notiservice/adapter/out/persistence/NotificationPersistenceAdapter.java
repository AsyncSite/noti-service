package com.asyncsite.notiservice.adapter.out.persistence;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationEntity;
import com.asyncsite.notiservice.adapter.out.persistence.repository.NotificationRepository;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import com.asyncsite.notiservice.domain.port.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public boolean updateNotificationWithCAS(String notificationId, Long expectedVersion, Notification notification) {
        int updatedCount = notificationRepository.updateStatusWithCAS(
                notificationId,
                expectedVersion,
                notification.getStatus(),
                LocalDateTime.now()
        );

        boolean success = updatedCount > 0;
        if (success) {
            log.debug("CAS update successful for notification: {} with version: {}", notificationId, expectedVersion);
        } else {
            log.warn("CAS update failed for notification: {} with version: {} - version mismatch or not found", notificationId, expectedVersion);
        }

        return success;
    }

    @Override
    @Transactional
    public List<Notification> findAndLockScheduledNotifications(int limit) {
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(0, limit);

        List<NotificationEntity> scheduledNotifications = notificationRepository
                .findScheduledNotificationsToProcess(NotificationStatus.SCHEDULED, now, pageRequest);

        if (!scheduledNotifications.isEmpty()) {
            List<String> notificationIds = scheduledNotifications.stream()
                    .map(NotificationEntity::getNotificationId)
                    .toList();

            int updatedCount = notificationRepository.markScheduledAsPending(
                    notificationIds,
                    NotificationStatus.PENDING,
                    NotificationStatus.SCHEDULED,
                    now
            );
            log.info("Marked {} scheduled notifications as PENDING", updatedCount);
        }

        return scheduledNotifications.stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findPendingNotifications(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return notificationRepository.findPendingNotifications(NotificationStatus.PENDING, pageRequest)
                .stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }
}
