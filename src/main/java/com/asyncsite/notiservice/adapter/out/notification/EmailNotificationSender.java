package com.asyncsite.notiservice.adapter.out.notification;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    public CompletableFuture<NotificationChannel> sendNotification(NotificationChannel channel, String title, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("이메일 발송 시작: recipient={}, channelId={}", channel.getRecipient(), channel.getChannelId());

                if (channel.getRecipient() == null || channel.getRecipient().isEmpty()) {
                    log.warn("수신자 이메일이 없습니다: channelId={}", channel.getChannelId());
                    return channel.withStatus(NotificationChannel.Status.FAILED)
                            .withErrorMessage("수신자 이메일이 없습니다.");
                }

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(getFromEmail());
                message.setTo(channel.getRecipient());
                message.setSubject(title != null ? title : "알림");
                message.setText(content != null ? content : "알림 내용");

                mailSender.send(message);

                log.info("이메일 발송 성공: recipient={}, channelId={}", channel.getRecipient(), channel.getChannelId());

                return channel.withStatus(NotificationChannel.Status.SENT)
                        .withSentAt(LocalDateTime.now());

            } catch (Exception e) {
                log.error("이메일 발송 실패: recipient={}, channelId={}", channel.getRecipient(), channel.getChannelId(), e);
                return channel.withStatus(NotificationChannel.Status.FAILED)
                        .withErrorMessage(e.getMessage());
            }
        });
    }

    @Override
    public boolean supportsChannelType(NotificationChannel.ChannelType channelType) {
        return channelType == NotificationChannel.ChannelType.EMAIL;
    }

    private String getFromEmail() {
        return fromEmail != null && !fromEmail.isEmpty() ? fromEmail : defaultFromEmail;
    }
} 