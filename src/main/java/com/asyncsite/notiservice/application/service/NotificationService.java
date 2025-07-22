package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.port.in.GetNotificationUseCase;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import com.asyncsite.notiservice.domain.port.in.SendNotificationUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        return null;
    }




















    @Async
    public CompletableFuture<Notification> sendNotification(String userId, String eventType, Map<String, Object> metadata) {
        try {
            log.info("알림 발송 시작: userId={}, eventType={}", userId, eventType);

            // 1. 알림 설정 조회
            NotificationSettings settings = settingsRepository.findByUserId(userId);
            if (!settings.isEventTypeEnabled(eventType)) {
                log.info("사용자가 해당 이벤트 타입을 비활성화함: userId={}, eventType={}", userId, eventType);
                return CompletableFuture.completedFuture(
                    Notification.createDisabled(userId, eventType, metadata)
                );
            }

            // 2. 방해금지 시간 확인
            if (settings.isInQuietHours()) {
                log.info("방해금지 시간입니다: userId={}", userId);
                return CompletableFuture.completedFuture(
                    Notification.createDisabled(userId, eventType, metadata)
                );
            }

            // 3. 기본 알림 생성
            Notification notification = Notification.createPending(userId, eventType, metadata);
            notification = notificationRepository.saveNotification(notification);

            // 4. 활성화된 채널들에 대해 알림 발송
            List<CompletableFuture<NotificationChannel>> sendFutures = new ArrayList<>();
            List<NotificationChannel> channels = new ArrayList<>();

            for (NotificationChannel.ChannelType channelType : NotificationChannel.ChannelType.values()) {
                if (settings.isChannelEnabled(channelType)) {
                    Optional<NotificationTemplate> templateOpt = templateRepository
                            .findTemplateByEventAndChannel(eventType, channelType, settings.getLanguage());

                    if (templateOpt.isPresent()) {
                        NotificationTemplate template = templateOpt.get();

                        // 템플릿 렌더링으로 알림 내용 업데이트
                        String title = template.renderTitle(metadata);
                        String content = template.renderContent(metadata);
                        notification = notification.updateContent(title, content);

                        // 채널 생성
                        NotificationChannel channel = NotificationChannel.create(
                                notification.getNotificationId(),
                                channelType,
                                getRecipientForChannel(channelType, metadata)
                        );
                        channels.add(channel);

                        // 발송 작업 생성
                        Optional<NotificationSenderPort> senderOpt = notificationSenders.stream()
                                .filter(sender -> sender.supportsChannelType(channelType))
                                .findFirst();

                        if (senderOpt.isPresent()) {
                            NotificationSenderPort sender = senderOpt.get();
                            CompletableFuture<NotificationChannel> sendFuture = sender.sendNotification(
                                    channel, title, content
                            ).thenApply(result -> {
                                if (result.isSent()) {
                                    return result;
                                } else {
                                    return result.markAsFailed("발송 실패");
                                }
                            }).exceptionally(ex -> {
                                log.error("채널 발송 중 오류 발생: channelType={}", channelType, ex);
                                return channel.markAsFailed(ex.getMessage());
                            });

                            sendFutures.add(sendFuture);
                        } else {
                            log.warn("지원되지 않는 채널 타입: {}", channelType);
                            channels.add(channel.markAsFailed("지원되지 않는 채널"));
                        }
                    } else {
                        log.warn("템플릿을 찾을 수 없음: eventType={}, channelType={}, language={}",
                                eventType, channelType, settings.getLanguage());
                    }
                }
            }

            // 5. 채널 정보를 알림에 추가
            Notification finalNotification = notification.addChannels(channels);
            finalNotification = notificationRepository.saveNotification(finalNotification);

            // 6. 모든 발송 작업 완료 대기 및 상태 업데이트
            Notification savedNotification = finalNotification;
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

                        Notification updatedNotification = allSuccess ?
                                savedNotification.markAsSent() :
                                savedNotification.markAsFailed("일부 또는 전체 채널 발송 실패");

                        notificationRepository.saveNotification(updatedNotification);
                        log.info("알림 발송 완료: notificationId={}, status={}",
                                updatedNotification.getNotificationId(), updatedNotification.getStatus());
                    });

            return CompletableFuture.completedFuture(savedNotification);

        } catch (Exception e) {
            log.error("알림 발송 중 예외 발생: userId={}, eventType={}", userId, eventType, e);
            return CompletableFuture.completedFuture(
                Notification.createFailed(userId, eventType, metadata, e.getMessage())
            );
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

            // 재시도 준비
            notification = notification.prepareRetry();
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
}
