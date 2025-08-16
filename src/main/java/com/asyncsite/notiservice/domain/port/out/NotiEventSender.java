package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.Notification;

public interface NotiEventSender {

    void notiCreated(Notification notification);
}
