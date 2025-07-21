package com.asyncsite.notiservice.adapter.out.notification;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordNotificationSender implements NotificationSenderPort {

    private final WebClient webClient;

    @Value("${application.notification.discord.webhook-url:}")
    private String webhookUrl;

    @Override
    public CompletableFuture<NotificationChannel> sendNotification(NotificationChannel channel, String title, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Discord 알림 발송 시작: recipient={}, channelId={}", channel.getRecipient(), channel.getChannelId());

                if (webhookUrl == null || webhookUrl.isEmpty()) {
                    log.warn("Discord webhook URL이 설정되지 않았습니다.");
                    return channel.withStatus(NotificationChannel.Status.FAILED)
                            .withErrorMessage("Discord webhook URL이 설정되지 않았습니다.");
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("content", content != null ? content : "알림 내용");

                String response = webClient.post()
                        .uri(webhookUrl)
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("Discord 알림 발송 성공: recipient={}, channelId={}, response={}", 
                        channel.getRecipient(), channel.getChannelId(), response);

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("response", response);

                return channel.withStatus(NotificationChannel.Status.SENT)
                        .withSentAt(LocalDateTime.now())
                        .withResponseData(responseData);

            } catch (Exception e) {
                log.error("Discord 알림 발송 실패: recipient={}, channelId={}", 
                        channel.getRecipient(), channel.getChannelId(), e);
                return channel.withStatus(NotificationChannel.Status.FAILED)
                        .withErrorMessage(e.getMessage());
            }
        });
    }

    @Override
    public boolean supportsChannelType(NotificationChannel.ChannelType channelType) {
        return channelType == NotificationChannel.ChannelType.DISCORD;
    }
} 