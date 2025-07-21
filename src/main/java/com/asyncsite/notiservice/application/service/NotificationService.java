package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.port.in.GetNotificationUseCase;
import com.asyncsite.notiservice.domain.port.in.SendNotificationUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService implements SendNotificationUseCase, GetNotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationSettingsRepositoryPort settingsRepository;
    private final NotificationTemplateRepositoryPort templateRepository;
    private final List<NotificationSenderPort> notificationSenders;

    @Override
    @Async
    public CompletableFuture<Notification> sendNotification(String userId, String eventType, Map<String, Object> metadata) {
        try {
            log.info("알림 발송 시작: userId={}, eventType={}", userId, eventType);

            // 2. 알림 설정 조회
            NotificationSettings settings = settingsRepository.findByUserId(userId);
            if (!settings.isEventTypeEnabled(eventType)) {
                log.info("사용자가 해당 이벤트 타입을 비활성화함: userId={}, eventType={}", userId, eventType);
                return CompletableFuture.completedFuture(createDisabledNotification(userId, eventType, metadata));
            }

            // 3. 템플릿 조회 및 알림 생성
            Notification notification = createNotification(userId, eventType, metadata);
            notification = notificationRepository.saveNotification(notification);

            // 4. 활성화된 채널들에 대해 알림 발송
            List<CompletableFuture<NotificationChannel>> sendFutures = new ArrayList<>();

            for (NotificationChannel.ChannelType channelType : NotificationChannel.ChannelType.values()) {
                if (settings.isChannelEnabled(channelType)) {
                    Optional<NotificationTemplate> templateOpt = templateRepository
                            .findTemplateByEventAndChannel(eventType, channelType, "ko");

                    if (templateOpt.isPresent()) {
                        NotificationTemplate template = templateOpt.get();
                        String title = template.renderTitle(metadata);
                        String content = template.renderContent(metadata);

                        NotificationChannel channel = NotificationChannel.builder()
                                .notificationId(notification.getNotificationId())
                                .channelType(channelType)
                                .recipient("")
                                .status(NotificationChannel.Status.PENDING)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        Optional<NotificationSenderPort> senderOpt = notificationSenders.stream()
                                .filter(sender -> sender.supportsChannelType(channelType))
                                .findFirst();

                        if (senderOpt.isPresent()) {
                            if (channelType == NotificationChannel.ChannelType.EMAIL) {
                                sendFutures.add(senderOpt.get().sendNotification(channel, title, content));
                            } else if (channelType == NotificationChannel.ChannelType.DISCORD) {
                                sendFutures.add(senderOpt.get().sendNotification(channel, null, content));
                            } else {
                                sendFutures.add(senderOpt.get().sendNotification(channel, null, content));
                            }
                        } else {
                            log.warn("지원하지 않는 채널 타입: {}", channelType);
                        }
                    }
                }
            }

            // 5. 모든 발송 완료 대기
            final Notification finalNotification = notification;
            CompletableFuture.allOf(sendFutures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        // 발송 결과에 따라 알림 상태 업데이트
                        boolean allSuccess = sendFutures.stream()
                                .allMatch(future -> {
                                    try {
                                        NotificationChannel result = future.get();
                                        return result.isSent();
                                    } catch (Exception e) {
                                        log.error("알림 발송 중 오류 발생", e);
                                        return false;
                                    }
                                });

                        Notification.NotificationStatus finalStatus = allSuccess ?
                                Notification.NotificationStatus.SENT : Notification.NotificationStatus.FAILED;

                        Notification updatedNotification = finalNotification.withStatus(finalStatus);
                        if (allSuccess) {
                            updatedNotification = updatedNotification.withSentAt(LocalDateTime.now());
                        }

                        notificationRepository.saveNotification(updatedNotification);
                        log.info("알림 발송 완료: notificationId={}, status={}",
                                updatedNotification.getNotificationId(), finalStatus);
                    });

            return CompletableFuture.completedFuture(notification);

        } catch (Exception e) {
            log.error("알림 발송 중 예외 발생: userId={}, eventType={}", userId, eventType, e);
            return CompletableFuture.completedFuture(createFailedNotification(userId, eventType, metadata, e.getMessage()));
        }
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

            // 재시도 횟수 증가
            notification = notification.withRetryCount(notification.getRetryCount() + 1);
            notification = notification.withStatus(Notification.NotificationStatus.RETRY);
            notification = notificationRepository.saveNotification(notification);

            // 원본 메타데이터로 재발송
            return sendNotification(notification.getUserId(), notification.getEventType(), notification.getMetadata());

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

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUserId(String userId, int page, int size) {
        return notificationRepository.findNotificationsByUserId(userId, page, size);
    }

    private Notification createNotification(String userId, String eventType, Map<String, Object> metadata) {
        return Notification.builder()
                .userId(userId)
                .eventType(eventType)
                .title("알림") // 템플릿에서 실제 제목으로 대체됨
                .content("알림 내용") // 템플릿에서 실제 내용으로 대체됨
                .metadata(metadata)
                .status(Notification.NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .retryCount(0)
                .channels(new ArrayList<>())
                .build();
    }

    private Notification createFailedNotification(String userId, String eventType, Map<String, Object> metadata, String errorMessage) {
        return Notification.builder()
                .userId(userId)
                .eventType(eventType)
                .title("알림 발송 실패")
                .content("알림 발송 중 오류가 발생했습니다.")
                .metadata(metadata)
                .status(Notification.NotificationStatus.FAILED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .retryCount(0)
                .errorMessage(errorMessage)
                .channels(new ArrayList<>())
                .build();
    }

    private Notification createDisabledNotification(String userId, String eventType, Map<String, Object> metadata) {
        return Notification.builder()
                .userId(userId)
                .eventType(eventType)
                .title("알림 비활성화")
                .content("사용자가 해당 알림을 비활성화했습니다.")
                .metadata(metadata)
                .status(Notification.NotificationStatus.SENT) // 비활성화된 알림은 성공으로 처리
                .createdAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .retryCount(0)
                .channels(new ArrayList<>())
                .build();
    }

    private String getRecipientForChannel(NotificationChannel.ChannelType channelType, Map<String, Object> userProfile) {
        return switch (channelType) {
            case EMAIL -> (String) userProfile.getOrDefault("email", "");
            case DISCORD -> (String) userProfile.getOrDefault("discordId", "");
            case PUSH -> (String) userProfile.getOrDefault("pushToken", "");
        };
    }
}
