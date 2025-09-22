package com.asyncsite.notiservice.adapter.out.event;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.event.NotificationCreated;
import com.asyncsite.notiservice.domain.port.out.NotiEventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
// @Component // DISABLED: Replaced by NotificationQueuePort to avoid optimistic locking
@RequiredArgsConstructor
public class NotiEventSenderImpl implements NotiEventSender {

    private  final ApplicationEventPublisher eventPublisher;

    @Override
    public void notiCreated(Notification notification) {
        log.info("Notification Create Event Push");
        eventPublisher.publishEvent(new NotificationCreated(notification));
    }
}
