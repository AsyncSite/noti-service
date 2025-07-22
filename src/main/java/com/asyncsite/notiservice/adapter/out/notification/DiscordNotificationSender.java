package com.asyncsite.notiservice.adapter.out.notification;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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

                // 채널별 webhook URL 사용 (recipient에 webhook URL이 있는 경우)
                String targetWebhookUrl = (channel.getRecipient() != null && !channel.getRecipient().isEmpty()) 
                        ? channel.getRecipient() : webhookUrl;

                if (targetWebhookUrl == null || targetWebhookUrl.isEmpty()) {
                    log.warn("Discord webhook URL이 설정되지 않았습니다.");
                    return channel.markAsFailed("Discord webhook URL이 설정되지 않았습니다.");
                }

                // Discord 메시지 페이로드 구성
                Map<String, Object> payload = new HashMap<>();
                if (title != null && !title.isEmpty()) {
                    // 제목과 내용이 모두 있는 경우 임베드 형식 사용
                    Map<String, Object> embed = new HashMap<>();
                    embed.put("title", title);
                    embed.put("description", content != null ? content : "알림 내용");
                    embed.put("color", 3447003); // 파란색
                    payload.put("embeds", new Object[]{embed});
                } else {
                    // 제목이 없는 경우 단순 텍스트
                    payload.put("content", content != null ? content : "알림 내용");
                }

                String response = webClient.post()
                        .uri(targetWebhookUrl)
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("Discord 알림 발송 성공: recipient={}, channelId={}, response={}", 
                        channel.getRecipient(), channel.getChannelId(), response);

                // 응답 데이터 구성
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("provider", "Discord");
                responseData.put("webhookUrl", targetWebhookUrl);
                responseData.put("response", response);
                responseData.put("payloadType", title != null && !title.isEmpty() ? "embed" : "text");
                responseData.put("messageLength", content != null ? content.length() : 0);

                return channel.markAsSent(response, responseData);

            } catch (Exception e) {
                log.error("Discord 알림 발송 실패: recipient={}, channelId={}", 
                        channel.getRecipient(), channel.getChannelId(), e);
                return channel.markAsFailed("Discord 발송 실패: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean supportsChannelType(NotificationChannel.ChannelType channelType) {
        return channelType == NotificationChannel.ChannelType.DISCORD;
    }
} 