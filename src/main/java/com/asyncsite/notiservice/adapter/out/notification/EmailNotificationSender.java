package com.asyncsite.notiservice.adapter.out.notification;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSenderPort {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    private final NotificationTemplateRepositoryPort templateRepository;

    @Value("${application.notification.email.from-address:}")
    private String configuredFromAddress;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Override
    public CompletableFuture<Notification> sendNotification(Notification notification) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("이메일 발송 시작: notificationId={}, userId={}",
                        notification.getNotificationId(), notification.getUserId());

                // 1. 템플릿 조회 및 렌더링
                Optional<NotificationTemplate> templateOpt = templateRepository
                        .findTemplateById(notification.getTemplateId());

                if (templateOpt.isEmpty()) {
                    log.warn("템플릿을 찾을 수 없습니다: templateId={}", notification.getTemplateId());
                    return markNotificationAsFailed(notification, "템플릿을 찾을 수 없습니다.");
                }

                NotificationTemplate template = templateOpt.get();

                // 템플릿이 EMAIL 채널에 맞는지 확인
                if (template.getChannelType() != ChannelType.EMAIL) {
                    log.warn("EMAIL 채널에 적합하지 않은 템플릿: templateId={}, channelType={}",
                            notification.getTemplateId(), template.getChannelType());
                    return markNotificationAsFailed(notification, "EMAIL 채널에 적합하지 않은 템플릿입니다.");
                }

                String title = notification.getTitle();
                String content = notification.getContent();

                // 2. 수신자 이메일 추출
                String recipientEmail = notification.getRecipientContact();

                if (recipientEmail == null || recipientEmail.isEmpty()) {
                    log.warn("수신자 이메일이 없습니다: notificationId={}", notification.getNotificationId());
                    return markNotificationAsFailed(notification, "수신자 이메일이 없습니다.");
                }

                // 3. 이메일 메시지 구성 및 발송
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                Context context = new Context();
                context.setVariable("title", title);
                context.setVariable("content", content);

                String html = templateEngine.process("email", context);

                String resolvedFrom = (configuredFromAddress != null && !configuredFromAddress.isBlank())
                        ? configuredFromAddress
                        : mailUsername;

                if (resolvedFrom == null || resolvedFrom.isBlank()) {
                    log.warn("발신자 이메일(from-address)이 비어 있습니다. 설정값을 확인해주세요. application.notification.email.from-address 또는 spring.mail.username");
                    throw new jakarta.mail.internet.AddressException("From address is empty");
                }

                helper.setFrom(resolvedFrom);
                helper.setTo(recipientEmail);
                helper.setSubject(title);
                helper.setText(html, true);
                mailSender.send(message);

                log.info("이메일 발송 성공: notificationId={}, recipient={}",
                        notification.getNotificationId(), recipientEmail);

                // 알림을 발송 완료 상태로 변경하여 반환
                notification.markAsSent();
                return notification;

            } catch (Exception e) {
                log.error("이메일 발송 실패: notificationId={}, userId={}",
                        notification.getNotificationId(), notification.getUserId(), e);
                return markNotificationAsFailed(notification, "이메일 발송 실패: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean supportsChannelType(ChannelType channelType) {
        return channelType == ChannelType.EMAIL;
    }

    /**
     * 알림을 실패 상태로 표시합니다.
     */
    private Notification markNotificationAsFailed(Notification notification, String errorMessage) {
        return notification.toBuilder()
                .status(com.asyncsite.notiservice.domain.model.vo.NotificationStatus.FAILED)
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }
}
