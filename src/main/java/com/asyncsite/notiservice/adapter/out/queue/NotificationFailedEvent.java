package com.asyncsite.notiservice.adapter.out.queue;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when notification exceeds max retry count
 */
public class NotificationFailedEvent extends ApplicationEvent {

    private final String notificationId;
    private final Exception error;

    public NotificationFailedEvent(String notificationId, Exception error) {
        super(notificationId);
        this.notificationId = notificationId;
        this.error = error;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public Exception getError() {
        return error;
    }
}