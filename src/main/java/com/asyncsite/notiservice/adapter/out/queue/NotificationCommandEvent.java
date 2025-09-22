package com.asyncsite.notiservice.adapter.out.queue;

import com.asyncsite.notiservice.domain.model.command.NotificationCommand;
import org.springframework.context.ApplicationEvent;

/**
 * Spring Event wrapper for NotificationCommand
 */
public class NotificationCommandEvent extends ApplicationEvent {

    private final NotificationCommand command;

    public NotificationCommandEvent(NotificationCommand command) {
        super(command);
        this.command = command;
    }

    public NotificationCommand getCommand() {
        return command;
    }
}