package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.dto.ChannelStatistics;
import com.asyncsite.notiservice.adapter.in.dto.NotificationChannelResponse;
import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.port.in.NotificationChannelUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification-channels")
@RequiredArgsConstructor
public class NotificationChannelController {

    private final NotificationChannelUseCase channelUseCase;

    @GetMapping
    public ResponseEntity<List<NotificationChannelResponse>> getChannels(
            @RequestParam(required = false) String notificationId,
            @RequestParam(required = false) String channelType) {

        log.info("채널 목록 조회: notificationId={}, channelType={}", notificationId, channelType);

        if (notificationId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<NotificationChannel> channels = channelUseCase.getChannelsByNotificationId(notificationId, channelType);
        List<NotificationChannelResponse> responses = channels.stream()
                .map(NotificationChannelResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<NotificationChannelResponse> getChannel(@PathVariable String channelId) {
        log.info("채널 조회: channelId={}", channelId);

        try {
            NotificationChannel channel = channelUseCase.getChannelById(channelId);
            NotificationChannelResponse response = NotificationChannelResponse.from(channel);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("채널 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{channelId}/retry")
    public ResponseEntity<NotificationChannelResponse> retryChannel(@PathVariable String channelId) {
        log.info("채널 재시도: channelId={}", channelId);

        try {
            NotificationChannel retriedChannel = channelUseCase.retryChannel(channelId);
            NotificationChannelResponse response = NotificationChannelResponse.from(retriedChannel);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("채널 재시도 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
