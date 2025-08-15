package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService implements NotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationSettingsRepositoryPort settingsRepository;
    private final NotificationTemplateRepositoryPort templateRepository;
    private final List<NotificationSenderPort> notificationSenders;

    @Override
    public List<Notification> getNotificationsByUserId(String userId, ChannelType channelType, int page, int size) {
        return notificationRepository.findNotificationsByUserId(userId, channelType, page, size);
    }

    @Override
    public CompletableFuture<Notification> sendNotification(String userId, ChannelType channelType, EventType eventType, Map<String, Object> metadata, String recipientContact) {
        log.info("알림 발송 시작: userId={}, channelType={}", userId, channelType);

        // 1. 알림 설정 조회 (기본값으로 처리)
        Optional<NotificationSettings> settingsOpt = settingsRepository.findByUserId(userId);
        NotificationSettings settings = settingsOpt.orElse(NotificationSettings.createDefault(userId));
        // TODO setting에 따른 알림 취소 처리
        // 2. 템플릿 선택: templateId가 있으면 우선 사용, 없으면 (channel,event) 규칙으로 선택
        Map<String, Object> variables = (Map<String, Object>) metadata.getOrDefault("variables", java.util.Map.of());
        variables = com.asyncsite.notiservice.common.MaskingUtil.maskVariablesForDisplay(variables);
        NotificationTemplate useTemplate;

        String templateId = (String) metadata.get("templateId");
        if (templateId != null && !templateId.isBlank()) {
            Optional<NotificationTemplate> templateOpt = templateRepository.findTemplateById(templateId);
            if (templateOpt.isEmpty()) {
                throw new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + templateId);
            }
            useTemplate = templateOpt.get();
        } else {
            // 기본 템플릿 우선
            Optional<NotificationTemplate> defaultOpt = templateRepository.findDefaultTemplate(channelType, eventType);
            if (defaultOpt.isPresent()) {
                useTemplate = defaultOpt.get();
            } else {
                // 우선순위/최신순 폴백
                List<NotificationTemplate> candidates = templateRepository.findActiveTemplatesByChannelAndEvent(channelType, eventType);
                if (candidates.isEmpty()) {
                    throw new IllegalArgumentException("해당 채널/이벤트의 활성 템플릿이 없습니다.");
                }
                useTemplate = candidates.get(0);
            }
        }

        // 채널 일치/활성 검증
        if (useTemplate.getChannelType() != channelType) {
            throw new IllegalArgumentException("요청 채널과 템플릿 채널이 일치하지 않습니다.");
        }
        if (!useTemplate.isActive()) {
            throw new IllegalArgumentException("비활성화된 템플릿입니다: " + useTemplate.getTemplateId());
        }

        String title = useTemplate.renderTitle(variables);
        String content = useTemplate.renderContent(variables);
        // 3. 알림 생성
        Notification notification = Notification.create(
                userId,
                useTemplate.getTemplateId(),
                channelType,
                title,
                content,
                recipientContact
        );

        notification = notificationRepository.saveNotification(notification);
        return sendNotification(notification);
    }

    @Override
    @Async
    public CompletableFuture<Notification> retryNotification(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findNotificationById(notificationId);
        if (notificationOpt.isEmpty()) {
            log.warn("재시도할 알림을 찾을 수 없음: notificationId={}", notificationId);
            return CompletableFuture.completedFuture(null);
        }
        return sendNotification(notificationOpt.get());
    }

    private CompletableFuture<Notification> sendNotification(Notification notification) {
        // 적절한 Sender 찾기 및 재발송
        Optional<NotificationSenderPort> senderOpt = notificationSenders.stream()
                .filter(sender -> sender.supportsChannelType(notification.getChannelType()))
                .findFirst();

        if (senderOpt.isEmpty()) {
            log.warn("지원되지 않는 채널 타입: {}", notification.getChannelType());
            return CompletableFuture.completedFuture(
                    updateNotificationToFailed(notification, "지원되지 않는 채널 타입입니다.")
            );
        }

        NotificationSenderPort sender = senderOpt.get();

        // 5. 비동기 발송 실행
        return sender.sendNotification(notification)
                .thenApply(result -> {
                    // 발송 결과 저장
                    Notification updatedNotification = notificationRepository.saveNotification(result);
                    log.info("알림 발송 완료: notificationId={}, status={}",
                            updatedNotification.getNotificationId(), updatedNotification.getStatus());
                    return updatedNotification;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> getNotificationById(String notificationId) {
        return notificationRepository.findNotificationById(notificationId);
    }

    /**
     * 알림을 실패 상태로 업데이트합니다.
     */
    private Notification updateNotificationToFailed(Notification notification, String errorMessage) {
        notification.fail();
        return notificationRepository.saveNotification(notification);
    }
}
