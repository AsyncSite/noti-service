package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.NotificationChannel;

import java.util.concurrent.CompletableFuture;

public interface NotificationSenderPort {
    
    /**
     * 알림을 발송합니다.
     * 
     * @param channel 발송할 채널 정보
     * @return 발송 결과
     */
    CompletableFuture<NotificationChannel> sendNotification(NotificationChannel channel, String title, String content);
    
    /**
     * 채널 타입을 지원하는지 확인합니다.
     * 
     * @param channelType 채널 타입
     * @return 지원 여부
     */
    boolean supportsChannelType(NotificationChannel.ChannelType channelType);
} 