package com.asyncsite.notiservice.domain.model.command;

import java.util.Map;

/**
 * Notification processing command
 * Used for queue-based notification processing to avoid optimistic locking issues
 */
public record NotificationCommand(
    String notificationId,
    CommandType type,
    Map<String, Object> metadata,
    Integer attemptCount
) {

    /**
     * Create a new send command
     */
    public static NotificationCommand createSendCommand(String notificationId) {
        return new NotificationCommand(
            notificationId,
            CommandType.SEND,
            Map.of(),
            0
        );
    }

    /**
     * Create a retry command
     */
    public static NotificationCommand createRetryCommand(String notificationId, Integer attemptCount) {
        return new NotificationCommand(
            notificationId,
            CommandType.RETRY,
            Map.of(),
            attemptCount
        );
    }

    /**
     * Increment attempt count for retry
     */
    public NotificationCommand withIncrementedAttempt() {
        return new NotificationCommand(
            this.notificationId,
            this.type,
            this.metadata,
            this.attemptCount + 1
        );
    }

    /**
     * Command types
     */
    public enum CommandType {
        SEND,       // Initial send
        RETRY,      // Retry after failure
        CANCEL      // Cancel notification
    }
}