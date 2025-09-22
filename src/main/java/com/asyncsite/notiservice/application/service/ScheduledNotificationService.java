package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.command.NotificationCommand;
import com.asyncsite.notiservice.domain.port.in.ScheduledNotificationUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationQueuePort;
import com.asyncsite.notiservice.domain.port.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service implementation for processing scheduled notifications.
 * Manages transaction boundaries for scheduled notification processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledNotificationService implements ScheduledNotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationQueuePort notificationQueue;

    @Value("${notification.scheduler.batch-size:100}")
    private int batchSize;

    @Value("${notification.scheduler.pending-reprocess-limit:50}")
    private int pendingReprocessLimit;

    /**
     * Process scheduled notifications within a transaction.
     * This ensures that the event publishing happens within a transaction context,
     * allowing @TransactionalEventListener to work properly.
     */
    @Override
    @Transactional
    public void processScheduledNotifications() {
        log.debug("Processing scheduled notifications within transaction");

        try {
            // Find and lock scheduled notifications atomically
            // This method updates status from SCHEDULED to PENDING in the database
            List<Notification> scheduledNotifications = notificationRepository
                    .findAndLockScheduledNotifications(batchSize);

            if (scheduledNotifications.isEmpty()) {
                log.debug("No scheduled notifications to process");
                return;
            }

            log.info("Found {} scheduled notifications to process", scheduledNotifications.size());

            // Queue each notification for processing
            // Since we're in a transaction, TransactionalEventListener will work
            for (Notification notification : scheduledNotifications) {
                try {
                    queueNotificationForProcessing(notification);
                } catch (Exception e) {
                    log.error("Failed to queue scheduled notification: {}",
                            notification.getNotificationId(), e);
                    // Continue processing other notifications even if one fails
                }
            }

            log.info("Successfully queued {} scheduled notifications", scheduledNotifications.size());

        } catch (Exception e) {
            log.error("Error processing scheduled notifications", e);
            // Transaction will rollback on exception
            throw e;
        }
    }

    /**
     * Reprocess notifications that have been stuck in PENDING state.
     * This is a safety mechanism for notifications that failed to send.
     */
    @Override
    @Transactional
    public void processPendingNotifications() {
        log.debug("Processing pending notifications for retry");

        try {
            List<Notification> pendingNotifications = notificationRepository
                    .findPendingNotifications(pendingReprocessLimit);

            if (pendingNotifications.isEmpty()) {
                log.debug("No pending notifications to reprocess");
                return;
            }

            log.info("Found {} pending notifications for reprocessing", pendingNotifications.size());

            for (Notification notification : pendingNotifications) {
                // Only reprocess if it's been pending for too long (e.g., > 5 minutes)
                if (notification.hasBeenPendingTooLong()) {
                    try {
                        queueNotificationForProcessing(notification);
                        log.debug("Requeued pending notification: {}", notification.getNotificationId());
                    } catch (Exception e) {
                        log.error("Failed to requeue pending notification: {}",
                                notification.getNotificationId(), e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error processing pending notifications", e);
            // Transaction will rollback on exception
            throw e;
        }
    }

    /**
     * Queue a notification for processing.
     * Creates a command and sends it to the notification queue.
     */
    private void queueNotificationForProcessing(Notification notification) {
        NotificationCommand command = new NotificationCommand(
                notification.getNotificationId(),
                NotificationCommand.CommandType.SEND,
                Map.of(
                    "source", "scheduler",
                    "scheduledAt", notification.getScheduledAt() != null
                        ? notification.getScheduledAt().toString()
                        : "immediate"
                ),
                0
        );

        notificationQueue.send(command);
        log.debug("Queued notification for processing: {}", notification.getNotificationId());
    }
}