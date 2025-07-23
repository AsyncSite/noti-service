package com.asyncsite.notiservice.adapter.out.notification;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSenderPort {

    private final JavaMailSender mailSender;
    private final NotificationTemplateRepositoryPort templateRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.notification.email.from-address:noreply@asyncsite.com}")
    private String defaultFromEmail;

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

                String title = template.renderTitle(notification.getMetadata());
                String content = template.renderContent(notification.getMetadata());

                // 2. 수신자 이메일 추출
                String recipientEmail = getRecipientEmail(notification.getMetadata());

                if (recipientEmail == null || recipientEmail.isEmpty()) {
                    log.warn("수신자 이메일이 없습니다: notificationId={}", notification.getNotificationId());
                    return markNotificationAsFailed(notification, "수신자 이메일이 없습니다.");
                }

                // 3. 이메일 메시지 구성 및 발송
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(getFromEmail());
                message.setTo(recipientEmail);
                message.setSubject(title != null && !title.trim().isEmpty() ? title : "알림");
                message.setText(content != null ? content : "알림 내용");

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
     * 수신자 이메일 주소를 추출합니다.
     */
    private String getRecipientEmail(Map<String, Object> metadata) {
        // metadata에서 이메일 주소를 찾습니다
        Object emailFromMetadata = metadata.get("email");
        if (emailFromMetadata != null && !emailFromMetadata.toString().isEmpty()) {
            return emailFromMetadata.toString();
        }

        Object recipientFromMetadata = metadata.get("recipient");
        if (recipientFromMetadata != null && !recipientFromMetadata.toString().isEmpty()) {
            return recipientFromMetadata.toString();
        }

        // userEmail 키도 확인해봅니다
        Object userEmailFromMetadata = metadata.get("userEmail");
        if (userEmailFromMetadata != null && !userEmailFromMetadata.toString().isEmpty()) {
            return userEmailFromMetadata.toString();
        }

        return null;
    }

    /**
     * 발신자 이메일 주소를 결정합니다.
     */
    private String getFromEmail() {
        return fromEmail != null && !fromEmail.isEmpty() ? fromEmail : defaultFromEmail;
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
