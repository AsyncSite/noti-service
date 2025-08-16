package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import jakarta.mail.MessagingException;

public interface NotificationSenderPort {

    /**
     * 알림을 발송합니다.
     *
     * @param notification 발송할 정보
     * @return 발송 결과
     */
    Notification sendNotification(Notification notification) throws MessagingException;
    boolean supportsChannelType(ChannelType channelType);
}
