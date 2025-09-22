package com.asyncsite.notiservice.adapter.in.scheduler;

import com.asyncsite.notiservice.domain.port.in.ScheduledNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler component that triggers scheduled notification processing.
 * This is an adapter that delegates actual business logic to the use case.
 * No transaction handling here - that's done in the use case implementation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ScheduledNotificationUseCase scheduledNotificationUseCase;

    @Value("${notification.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    /**
     * Process scheduled notifications every minute.
     * Delegates to the use case which handles transactions properly.
     */
    @Scheduled(fixedDelayString = "${notification.scheduler.interval:60000}")
    public void processScheduledNotifications() {
        if (!schedulerEnabled) {
            log.debug("Notification scheduler is disabled");
            return;
        }

        try {
            log.debug("Triggering scheduled notification processing");

            // Delegate to use case - transaction will be handled there
            scheduledNotificationUseCase.processScheduledNotifications();

            log.debug("Scheduled notification processing completed");

        } catch (Exception e) {
            log.error("Error processing scheduled notifications", e);
        }
    }

    /**
     * Process pending notifications that may have been missed or stuck.
     * Runs less frequently to catch any notifications that weren't processed normally.
     * Delegates to the use case which handles transactions properly.
     */
    @Scheduled(fixedDelayString = "${notification.scheduler.pending.interval:300000}")
    public void processPendingNotifications() {
        if (!schedulerEnabled) {
            return;
        }

        try {
            log.debug("Triggering pending notification cleanup");

            // Delegate to use case - transaction will be handled there
            scheduledNotificationUseCase.processPendingNotifications();

            log.debug("Pending notification cleanup completed");

        } catch (Exception e) {
            log.error("Error processing pending notifications", e);
        }
    }
}