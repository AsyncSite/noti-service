package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;

import java.util.concurrent.CompletableFuture;

public interface NotificationSenderPort {

    /**
     * 알림을 발송합니다.
     *
     * @param notification 발송할 정보
     * @return 발송 결과
     */
    CompletableFuture<Notification> sendNotification(Notification notification);
    boolean supportsChannelType(ChannelType channelType);
}
