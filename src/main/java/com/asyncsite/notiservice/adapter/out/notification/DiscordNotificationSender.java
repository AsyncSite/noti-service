package com.asyncsite.notiservice.adapter.out.notification;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordNotificationSender implements NotificationSenderPort {

    private final WebClient webClient;
    private final NotificationTemplateRepositoryPort templateRepository;

    @Value("${application.notification.discord.webhook-url:}")
    private String webhookUrl;

    @Override
    public CompletableFuture<Notification> sendNotification(Notification notification) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Discord 알림 발송 시작: notificationId={}, userId={}",
                        notification.getNotificationId(), notification.getUserId());

                // 1. 템플릿 조회 및 렌더링
                Optional<NotificationTemplate> templateOpt = templateRepository
                        .findTemplateById(notification.getTemplateId());

                if (templateOpt.isEmpty()) {
                    log.warn("템플릿을 찾을 수 없습니다: templateId={}", notification.getTemplateId());
                    return markNotificationAsFailed(notification, "템플릿을 찾을 수 없습니다.");
                }

                NotificationTemplate template = templateOpt.get();

                // 템플릿이 Discord 채널에 맞는지 확인
                if (template.getChannelType() != ChannelType.DISCORD) {
                    log.warn("Discord 채널에 적합하지 않은 템플릿: templateId={}, channelType={}",
                            notification.getTemplateId(), template.getChannelType());
                    return markNotificationAsFailed(notification, "Discord 채널에 적합하지 않은 템플릿입니다.");
                }

                String title = notification.getTitle();
                String content = notification.getContent();

                // 2. 수신자 정보 추출 (metadata에서 webhook URL 또는 설정값 사용)
                List<String> targetWebhookUrl = notification.getRecipientContacts();

                if (targetWebhookUrl == null || targetWebhookUrl.isEmpty()) {
                    log.warn("Discord webhook URL이 설정되지 않았습니다.");
                    return markNotificationAsFailed(notification, "Discord webhook URL이 설정되지 않았습니다.");
                }

                // 3. Discord 메시지 페이로드 구성
                Map<String, Object> payload = createDiscordPayload(title, content);

                // 4. Discord API 호출
                String response = webClient.post()
                        .uri(targetWebhookUrl.getFirst())
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("Discord 알림 발송 성공: notificationId={}, response={}",
                        notification.getNotificationId(), response);

                // 알림을 발송 완료 상태로 변경하여 반환
                notification.markAsSent();
                return notification;

            } catch (Exception e) {
                log.error("Discord 알림 발송 실패: notificationId={}, userId={}",
                        notification.getNotificationId(), notification.getUserId(), e);
                return markNotificationAsFailed(notification, "Discord 발송 실패: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean supportsChannelType(ChannelType channelType) {
        return channelType == ChannelType.DISCORD;
    }

    /**
     * Discord 메시지 페이로드를 생성합니다.
     */
    private Map<String, Object> createDiscordPayload(String title, String content) {
        Map<String, Object> payload = new HashMap<>();

        if (title != null && !title.trim().isEmpty()) {
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

        return payload;
    }

    /**
     * 알림을 실패 상태로 표시합니다.
     */
    private Notification markNotificationAsFailed(Notification notification, String errorMessage) {
        // Notification 도메인에 markAsFailed 메서드가 있다면 사용하고, 없다면 기본 상태 변경 로직 구현
        return notification.toBuilder()
                .status(com.asyncsite.notiservice.domain.model.vo.NotificationStatus.FAILED)
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }
}
