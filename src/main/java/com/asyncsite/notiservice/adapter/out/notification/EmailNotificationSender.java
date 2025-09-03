package com.asyncsite.notiservice.adapter.out.notification;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSenderPort {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    private final NotificationTemplateRepositoryPort templateRepository;

    @Value("${application.notification.email.from-address:}")
    private String configuredFromAddress;

    @Value("${application.notification.email.from-name:AsyncSite}")
    private String configuredFromName;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Override
    public Notification sendNotification(Notification notification) throws MessagingException, UnsupportedEncodingException {
        log.info("이메일 발송 시작: notificationId={}, userId={}",
                notification.getNotificationId(), notification.getUserId());

        // 1. 템플릿 조회 및 렌더링
        Optional<NotificationTemplate> templateOpt = templateRepository
                .findTemplateById(notification.getTemplateId());

        if (templateOpt.isEmpty()) {
            log.warn("템플릿을 찾을 수 없습니다: templateId={}", notification.getTemplateId());
            notification.fail("템플릿을 찾을 수 없습니다.");
            return notification;
        }

        NotificationTemplate template = templateOpt.get();

        // 템플릿이 EMAIL 채널에 맞는지 확인
        if (template.getChannelType() != ChannelType.EMAIL) {
            log.warn("EMAIL 채널에 적합하지 않은 템플릿: templateId={}, channelType={}",
                    notification.getTemplateId(), template.getChannelType());
            notification.fail("EMAIL 채널에 적합하지 않은 템플릿입니다.");
            return notification;
        }

        String title = notification.getTitle();
        String content = notification.getContent();

        // 2. 수신자 이메일 추출
        List<String> recipientEmails = notification.getRecipientContacts();

        if (recipientEmails == null || recipientEmails.isEmpty()) {
            log.warn("수신자 이메일이 없습니다: notificationId={}", notification.getNotificationId());
            notification.fail("수신자 이메일이 없습니다.");
            return notification;
        }

        // 3. 이메일 메시지 구성 및 발송
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("content", content);

        // 디버깅: 템플릿 처리 전 로그
        log.info("템플릿 처리 시작 - templateEngine: {}, context variables: title={}, content length={}",
            templateEngine.getClass().getName(),
            title != null ? title.substring(0, Math.min(title.length(), 50)) : "null",
            content != null ? content.length() : 0);

        String html = null;
        try {
            html = templateEngine.process("email", context);
            log.info("템플릿 처리 성공 - HTML 길이: {}", html != null ? html.length() : 0);
        } catch (Exception e) {
            log.error("템플릿 처리 실패 - 상세 에러: ", e);
            // 템플릿 리졸버 정보 출력
            log.error("Template Engine Configuration: {}", templateEngine.getConfiguration());
            throw e;
        }

        String resolvedFromAddress = (configuredFromAddress != null && !configuredFromAddress.isBlank())
                ? configuredFromAddress
                : mailUsername;

        if (resolvedFromAddress == null || resolvedFromAddress.isBlank()) {
            log.warn("발신자 이메일(from-address)이 비어 있습니다. 설정값을 확인해주세요. application.notification.email.from-address 또는 spring.mail.username");
            throw new jakarta.mail.internet.AddressException("From address is empty");
        }

        // 발신자 이름과 이메일 주소를 함께 설정
        String resolvedFromName = (configuredFromName != null && !configuredFromName.isBlank())
                ? configuredFromName
                : "AsyncSite";
        
        log.info("이메일 발신자 설정: fromName='{}', fromAddress='{}'", resolvedFromName, resolvedFromAddress);
        helper.setFrom(resolvedFromAddress, resolvedFromName);
        helper.setTo(recipientEmails.toArray(new String[0]));
        helper.setSubject(title);
        helper.setText(html, true);
        mailSender.send(message);

        log.info("이메일 발송 성공: notificationId={}, recipient={}", notification.getNotificationId(), recipientEmails);

        // 알림을 발송 완료 상태로 변경하여 반환
        notification.markAsSent();
        return notification;
    }

    @Override
    public boolean supportsChannelType(ChannelType channelType) {
        return channelType == ChannelType.EMAIL;
    }

}
