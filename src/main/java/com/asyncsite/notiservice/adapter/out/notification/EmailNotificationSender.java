package com.asyncsite.notiservice.adapter.out.notification;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSenderPort {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.notification.email.from-address:noreply@asyncsite.com}")
    private String defaultFromEmail;

    @Override
    public CompletableFuture<Notification> sendNotification(Notification notification, String title, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("이메일 발송 시작: recipient={}, channelId={}", channel.getRecipient(), channel.getChannelId());

                if (channel.getRecipient() == null || channel.getRecipient().isEmpty()) {
                    log.warn("수신자 이메일이 없습니다: channelId={}", channel.getChannelId());
                    return channel.markAsFailed("수신자 이메일이 없습니다.");
                }

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(getFromEmail());
                message.setTo(channel.getRecipient());
                message.setSubject(title != null ? title : "알림");
                message.setText(content != null ? content : "알림 내용");

                mailSender.send(message);

                log.info("이메일 발송 성공: recipient={}, channelId={}", channel.getRecipient(), channel.getChannelId());

                // 발송 성공 시 응답 데이터와 함께 상태 업데이트
                Map<String, Object> responseData = Map.of(
                        "provider", "JavaMailSender",
                        "sentAt", System.currentTimeMillis(),
                        "fromEmail", getFromEmail(),
                        "messageSize", content != null ? content.length() : 0
                );

                return channel.markAsSent(null, responseData);

            } catch (Exception e) {
                log.error("이메일 발송 실패: recipient={}, channelId={}", channel.getRecipient(), channel.getChannelId(), e);
                return channel.markAsFailed("이메일 발송 실패: " + e.getMessage());
            }
        });
    }

    private String getFromEmail() {
        return fromEmail != null && !fromEmail.isEmpty() ? fromEmail : defaultFromEmail;
    }
}
