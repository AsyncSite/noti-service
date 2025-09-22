package com.asyncsite.notiservice.domain.port.in;

/**
 * Use case for processing scheduled notifications.
 * This separates the scheduling concern from the main notification business logic.
 */
public interface ScheduledNotificationUseCase {

    /**
     * Process scheduled notifications that are ready to be sent.
     * Finds notifications scheduled for current time or earlier and queues them for sending.
     */
    void processScheduledNotifications();

    /**
     * Process notifications that have been in PENDING state for too long.
     * This handles cases where notifications got stuck in PENDING status.
     */
    void processPendingNotifications();
}