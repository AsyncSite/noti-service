package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
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
    public CompletableFuture<Notification> sendNotification(String userId, ChannelType channelType, Map<String, Object> metadata) {
        log.info("알림 발송 시작: userId={}, channelType={}", userId, channelType);

        // 1. 알림 설정 조회 (기본값으로 처리)
        Optional<NotificationSettings> settingsOpt = settingsRepository.findByUserId(userId);
        NotificationSettings settings = settingsOpt.orElse(NotificationSettings.createDefault(userId));
        // TODO setting에 따른 알림 취소 처리
        // 2. 채널별 템플릿 조회
        List<NotificationTemplate> templates = templateRepository.findTemplateByChannel(channelType);

        if (templates.isEmpty()) {
            log.warn("템플릿을 찾을 수 없음: channelType={}", channelType);
            throw new RuntimeException("템플릿을 찾을 수 없음");
        }

        // 첫 번째 활성화된 템플릿 사용
        NotificationTemplate template = templates.stream()
                .filter(NotificationTemplate::isActive)
                .findFirst()
                .orElse(templates.getFirst());

        // 3. 알림 생성
        Notification notification = Notification.create(
                userId,
                template.getTemplateId(),
                channelType,
                metadata
        );

        notification = notificationRepository.saveNotification(notification);

        // 적절한 Sender 찾기 및 재발송
        Notification finalNotification = notification;
        Optional<NotificationSenderPort> senderOpt = notificationSenders.stream()
                .filter(sender -> sender.supportsChannelType(finalNotification.getChannelType()))
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
                }); // CompletableFuture를 동기화
    }

    @Override
    @Async
    public CompletableFuture<Notification> retryNotification(String notificationId) {
        try {
            Optional<Notification> notificationOpt = notificationRepository.findNotificationById(notificationId);
            if (notificationOpt.isEmpty()) {
                log.warn("재시도할 알림을 찾을 수 없음: notificationId={}", notificationId);
                return CompletableFuture.completedFuture(null);
            }

            Notification notification = notificationOpt.get();
            if (!notification.canRetry()) {
                log.warn("재시도할 수 없는 알림: notificationId={}, status={}, retryCount={}",
                        notificationId, notification.getStatus(), notification.getRetryCount());
                return CompletableFuture.completedFuture(notification);
            }

            // 재시도 준비
            notification.prepareRetry();
            notificationRepository.saveNotification(notification);

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

            return sender.sendNotification(notification)
                    .thenApply(result -> {
                        Notification updatedNotification = notificationRepository.saveNotification(result);
                        log.info("알림 재시도 완료: notificationId={}, status={}",
                                updatedNotification.getNotificationId(), updatedNotification.getStatus());
                        return updatedNotification;
                    })
                    .exceptionally(ex -> {
                        log.error("알림 재시도 중 오류 발생: notificationId={}", notificationId, ex);
                        return updateNotificationToFailed(notification, "재시도 중 오류 발생: " + ex.getMessage());
                    });

        } catch (Exception e) {
            log.error("알림 재시도 중 예외 발생: notificationId={}", notificationId, e);
            return CompletableFuture.completedFuture(null);
        }
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
