package com.asyncsite.notiservice.adapter.in.event;

import com.asyncsite.notiservice.adapter.out.queue.InMemoryNotificationQueue;
import com.asyncsite.notiservice.adapter.out.queue.NotificationCommandEvent;
import com.asyncsite.notiservice.adapter.out.queue.NotificationFailedEvent;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.command.NotificationCommand;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import com.asyncsite.notiservice.domain.port.out.NotificationRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Optional;

/**
 * Handles NotificationCommand events asynchronously
 * Replaces the old NotiEventHandler to avoid optimistic locking issues
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCommandHandler {

    private final NotificationRepositoryPort notificationRepository;
    private final List<NotificationSenderPort> notificationSenders;
    private final InMemoryNotificationQueue notificationQueue;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCommand(NotificationCommandEvent event) {
        NotificationCommand command = event.getCommand();
        String notificationId = command.notificationId();

        log.info("Processing notification command: {}, type: {}, attempt: {}",
            notificationId, command.type(), command.attemptCount());

        try {
            // Retrieve notification by ID (fresh from DB to avoid version issues)
            Optional<Notification> notificationOpt = notificationRepository.findNotificationById(notificationId);

            if (notificationOpt.isEmpty()) {
                log.error("Notification not found: {}", notificationId);
                return;
            }

            Notification notification = notificationOpt.get();

            // Skip if already sent successfully
            if (notification.isSent()) {
                log.info("Notification already sent successfully: {}", notificationId);
                return;
            }

            // Find appropriate sender
            Optional<NotificationSenderPort> senderOpt = notificationSenders.stream()
                .filter(sender -> sender.supportsChannelType(notification.getChannelType()))
                .findFirst();

            if (senderOpt.isEmpty()) {
                log.error("No sender found for channel type: {}", notification.getChannelType());
                notification.fail("No sender available for channel: " + notification.getChannelType());
                notificationRepository.saveNotification(notification);
                return;
            }

            // Send notification
            NotificationSenderPort sender = senderOpt.get();
            Notification sentNotification = sender.sendNotification(notification);

            // Save updated status
            notificationRepository.saveNotification(sentNotification);

            // Clear failure count on success
            if (sentNotification.isSent()) {
                notificationQueue.clearFailureCount(notificationId);
                log.info("Notification sent successfully: {}", notificationId);
            }

        } catch (Exception e) {
            log.error("Failed to process notification command: {}", notificationId, e);

            // Send to DLQ for retry with exponential backoff
            notificationQueue.sendToDLQ(command, e);
        }
    }

    @EventListener
    public void handleNotificationFailed(NotificationFailedEvent event) {
        String notificationId = event.getNotificationId();
        Exception error = event.getError();

        log.error("Notification permanently failed after max retries: {}", notificationId, error);

        try {
            // Mark notification as permanently failed
            Optional<Notification> notificationOpt = notificationRepository.findNotificationById(notificationId);
            if (notificationOpt.isPresent()) {
                Notification notification = notificationOpt.get();
                notification.fail("Max retry attempts exceeded: " + error.getMessage());
                notificationRepository.saveNotification(notification);
            }
        } catch (Exception e) {
            log.error("Failed to update notification status to failed: {}", notificationId, e);
        }
    }
}