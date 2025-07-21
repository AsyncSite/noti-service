package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.adapter.in.dto.ChannelStatistics;
import com.asyncsite.notiservice.adapter.in.dto.ChannelStatistics.DailyChannelStats;

import java.time.LocalDate;
import java.util.List;

public interface NotificationChannelUseCase {

    /**
     * 알림 ID로 채널 목록을 조회합니다.
     */
    List<NotificationChannel> getChannelsByNotificationId(String notificationId, String channelType);

    /**
     * 채널 ID로 채널을 조회합니다.
     */
    NotificationChannel getChannelById(String channelId);

    /**
     * 채널 재시도를 수행합니다.
     */
    NotificationChannel retryChannel(String channelId);
}
