package com.asyncsite.notiservice.adapter.in.client;

import com.asyncsite.notiservice.domain.model.event.NotificationCreated;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotiEventHandler {
    //
    public final NotificationUseCase notificationUseCase;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(NotificationCreated event) throws MessagingException, UnsupportedEncodingException {
        log.info("Notification send Eevent FROM Notification Id: {}", event.notification().getNotificationId() );
        notificationUseCase.sendNotification(event.notification());
    }
}
