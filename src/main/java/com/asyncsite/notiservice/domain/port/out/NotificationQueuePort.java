package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.command.NotificationCommand;
import java.time.Duration;

/**
 * Port for notification queue operations
 * This abstraction allows switching between in-memory and actual message queue implementations
 */
public interface NotificationQueuePort {

    /**
     * Send a notification command to the queue
     * @param command The notification command to send
     */
    void send(NotificationCommand command);

    /**
     * Send a notification command with delay
     * @param command The notification command to send
     * @param delay The delay duration before processing
     */
    void sendDelayed(NotificationCommand command, Duration delay);

    /**
     * Send a failed notification command to the Dead Letter Queue
     * @param command The notification command that failed
     * @param error The exception that caused the failure
     */
    void sendToDLQ(NotificationCommand command, Exception error);
}