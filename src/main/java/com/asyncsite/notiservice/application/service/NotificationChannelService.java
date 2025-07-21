package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.adapter.in.dto.ChannelStatistics;
import com.asyncsite.notiservice.adapter.in.dto.ChannelStatistics.DailyChannelStats;
import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.port.in.NotificationChannelUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationChannelRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationChannelService implements NotificationChannelUseCase {

    private final NotificationChannelRepositoryPort channelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationChannel> getChannelsByNotificationId(String notificationId, String channelType) {
        log.info("알림 채널 목록 조회: notificationId={}, channelType={}", notificationId, channelType);

        return channelRepository.findByNotificationId(notificationId)
                .stream()
                .filter(channel -> channelType == null ||
                        channel.getChannelType().name().equalsIgnoreCase(channelType))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationChannel getChannelById(String channelId) {
        log.info("채널 조회: channelId={}", channelId);

        return channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다: " + channelId));
    }

    @Override
    public NotificationChannel retryChannel(String channelId) {
        log.info("채널 재시도: channelId={}", channelId);

        NotificationChannel channel = getChannelById(channelId);

        // 재시도 횟수 증가 및 상태 업데이트
        NotificationChannel updatedChannel = channel.toBuilder()
                .retryCount(channel.getRetryCount() + 1)
                .status(NotificationChannel.Status.PENDING)
                .updatedAt(LocalDateTime.now())
                .build();

        return channelRepository.save(updatedChannel);
    }
}
