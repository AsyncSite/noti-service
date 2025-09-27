package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.exception.NotificationDisabledException;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.NotificationSettings;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.command.NotificationCommand;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.model.vo.NotificationSearchCriteria;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import com.asyncsite.notiservice.domain.port.out.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService implements NotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationSettingsRepositoryPort settingsRepository;
    private final NotificationTemplateRepositoryPort templateRepository;
    private final List<NotificationSenderPort> notificationSenders;
    private final NotificationQueuePort notificationQueue;
    private final TemplateEngine templateEngine;

    @Override
    public List<Notification> getNotificationsByUserId(String userId, ChannelType channelType, int page, int size) {
        return notificationRepository.findNotificationsByUserId(userId, channelType, page, size);
    }

    @Override
    public Notification createNotification(String userId, ChannelType channelType, EventType eventType, Map<String, Object> metadata, String recipientContact) {
        return save(userId, channelType, eventType, metadata, List.of(recipientContact));
    }

    @Override
    public Notification createNotificationBulk(String userId, ChannelType channelType, EventType eventType, Map<String, Object> metadata, List<String> recipientContacts) {
        return save(userId, channelType, eventType, metadata, recipientContacts);
    }

    private Notification save(String userId, ChannelType channelType, EventType eventType, Map<String, Object> metadata, List<String> recipientContacts) {
        log.info("알림 발송 시작: userId={}, channelType={}", userId, channelType);
        
        // Trial user handling: skip settings check if userId is null
        NotificationSettings settings;
        if (userId == null || userId.isEmpty()) {
            // Trial user: use default settings (all notifications enabled)
            log.info("Trial user detected, using default notification settings");
            settings = NotificationSettings.createDefaultForTrial();
        } else {
            // Authenticated user: check user settings
            Optional<NotificationSettings> settingsOpt = settingsRepository.findByUserId(userId);
            settings = settingsOpt.orElse(NotificationSettings.createDefault(userId));
            // Check if notifications are enabled for this channel
            if (!settings.isNotificationEnabled(channelType)) {
                log.info("Notifications disabled for user: {} channel: {}", userId, channelType);
                throw new NotificationDisabledException(userId, channelType.name(),
                    String.format("사용자 %s의 %s 알림이 비활성화되어 있습니다", userId, channelType));
            }
        }
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
                recipientContacts
        );

        notification = notificationRepository.saveNotification(notification);

        // Send command with only ID to avoid optimistic locking
        NotificationCommand command = NotificationCommand.createSendCommand(notification.getNotificationId());
        notificationQueue.send(command);

        return notification;
    }

    @Override
    public Notification retryNotification(String notificationId) throws MessagingException, UnsupportedEncodingException {
        Optional<Notification> notificationOpt = notificationRepository.findNotificationById(notificationId);
        if (notificationOpt.isEmpty()) {
            log.warn("재시도할 알림을 찾을 수 없음: notificationId={}", notificationId);
            return null;
        }
        return sendNotification(notificationOpt.get());
    }

    @Override
    public Notification sendNotification(Notification notification) throws MessagingException, UnsupportedEncodingException {
        // 적절한 Sender 찾기 및 재발송
        Optional<NotificationSenderPort> senderOpt = notificationSenders.stream()
                .filter(sender -> sender.supportsChannelType(notification.getChannelType()))
                .findFirst();

        if (senderOpt.isEmpty()) {
            log.warn("지원되지 않는 채널 타입: {}", notification.getChannelType());
            throw new RuntimeException("지원되지 않는 채널 타입입니다.");
        }

        NotificationSenderPort sender = senderOpt.get();

        // 5. 비동기 발송 실행
        Notification sendNotification = sender.sendNotification(notification);
        notificationRepository.saveNotification(sendNotification);
        return sendNotification;
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(String notificationId) {
        return notificationRepository.findNotificationById(notificationId)
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Notification createScheduledNotification(String userId, ChannelType channelType, EventType eventType,
                                                  Map<String, Object> metadata, String recipientContact,
                                                  LocalDateTime scheduledAt) {
        return saveScheduled(userId, channelType, eventType, metadata, List.of(recipientContact), scheduledAt);
    }

    @Override
    public Notification createScheduledNotificationBulk(String userId, ChannelType channelType, EventType eventType,
                                                       Map<String, Object> metadata, List<String> recipientContacts,
                                                       LocalDateTime scheduledAt) {
        return saveScheduled(userId, channelType, eventType, metadata, recipientContacts, scheduledAt);
    }

    @Override
    public Notification createForceNotification(String userId, ChannelType channelType, EventType eventType,
                                               Map<String, Object> metadata, String recipientContact) {
        log.info("강제 알림 발송: userId={}, channelType={}, recipientContact={}", userId, channelType, recipientContact);
        return saveForce(userId, channelType, eventType, metadata, List.of(recipientContact));
    }

    @Override
    public Notification createForceScheduledNotification(String userId, ChannelType channelType, EventType eventType,
                                                        Map<String, Object> metadata, String recipientContact,
                                                        LocalDateTime scheduledAt) {
        log.info("강제 예약 알림 생성: userId={}, channelType={}, scheduledAt={}", userId, channelType, scheduledAt);
        return saveScheduledForce(userId, channelType, eventType, metadata, List.of(recipientContact), scheduledAt);
    }

    private Notification saveScheduled(String userId, ChannelType channelType, EventType eventType,
                                      Map<String, Object> metadata, List<String> recipientContacts,
                                      LocalDateTime scheduledAt) {
        log.info("예약 알림 생성: userId={}, channelType={}, scheduledAt={}", userId, channelType, scheduledAt);

        // Validate scheduled time is in the future
        if (scheduledAt != null && scheduledAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("예약 시간은 미래여야 합니다: " + scheduledAt);
        }

        // Trial user handling: skip settings check if userId is null
        NotificationSettings settings;
        if (userId == null || userId.isEmpty()) {
            log.info("Trial user detected, using default notification settings");
            settings = NotificationSettings.createDefaultForTrial();
        } else {
            Optional<NotificationSettings> settingsOpt = settingsRepository.findByUserId(userId);
            settings = settingsOpt.orElse(NotificationSettings.createDefault(userId));
            if (!settings.isNotificationEnabled(channelType)) {
                log.info("Notifications disabled for user: {} channel: {}", userId, channelType);
                return null;
            }
        }

        // Template selection
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
            Optional<NotificationTemplate> defaultOpt = templateRepository.findDefaultTemplate(channelType, eventType);
            if (defaultOpt.isPresent()) {
                useTemplate = defaultOpt.get();
            } else {
                List<NotificationTemplate> candidates = templateRepository.findActiveTemplatesByChannelAndEvent(channelType, eventType);
                if (candidates.isEmpty()) {
                    throw new IllegalArgumentException("해당 채널/이벤트의 활성 템플릿이 없습니다.");
                }
                useTemplate = candidates.get(0);
            }
        }

        // Validate template
        if (useTemplate.getChannelType() != channelType) {
            throw new IllegalArgumentException("요청 채널과 템플릿 채널이 일치하지 않습니다.");
        }
        if (!useTemplate.isActive()) {
            throw new IllegalArgumentException("비활성화된 템플릿입니다: " + useTemplate.getTemplateId());
        }

        String title = useTemplate.renderTitle(variables);
        String content = useTemplate.renderContent(variables);

        // Create scheduled notification
        Notification notification = Notification.createScheduled(
                userId,
                useTemplate.getTemplateId(),
                channelType,
                title,
                content,
                recipientContacts,
                scheduledAt
        );

        notification = notificationRepository.saveNotification(notification);

        // Scheduled notifications don't get queued immediately - the scheduler will handle them
        log.info("예약 알림 생성 완료: notificationId={}, scheduledAt={}",
                notification.getNotificationId(), notification.getScheduledAt());

        return notification;
    }

    private Notification saveForce(String userId, ChannelType channelType, EventType eventType,
                                  Map<String, Object> metadata, List<String> recipientContacts) {
        log.info("강제 알림 발송 처리 시작: userId={}, channelType={}", userId, channelType);

        // 템플릿 선택 (설정 체크 없이)
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

        // 알림 생성
        Notification notification = Notification.create(
                userId,
                useTemplate.getTemplateId(),
                channelType,
                title,
                content,
                recipientContacts
        );

        notification = notificationRepository.saveNotification(notification);

        // Send command with only ID to avoid optimistic locking
        NotificationCommand command = NotificationCommand.createSendCommand(notification.getNotificationId());
        notificationQueue.send(command);

        log.info("강제 알림 발송 완료: notificationId={}", notification.getNotificationId());
        return notification;
    }

    private Notification saveScheduledForce(String userId, ChannelType channelType, EventType eventType,
                                           Map<String, Object> metadata, List<String> recipientContacts,
                                           LocalDateTime scheduledAt) {
        log.info("강제 예약 알림 생성: userId={}, channelType={}, scheduledAt={}", userId, channelType, scheduledAt);

        // Validate scheduled time is in the future
        if (scheduledAt != null && scheduledAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("예약 시간은 미래여야 합니다: " + scheduledAt);
        }

        // Template selection (설정 체크 없이)
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
            Optional<NotificationTemplate> defaultOpt = templateRepository.findDefaultTemplate(channelType, eventType);
            if (defaultOpt.isPresent()) {
                useTemplate = defaultOpt.get();
            } else {
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

        // Create scheduled notification
        Notification notification = Notification.createScheduled(
                userId,
                useTemplate.getTemplateId(),
                channelType,
                title,
                content,
                recipientContacts,
                scheduledAt
        );

        notification = notificationRepository.saveNotification(notification);

        // Scheduled notifications don't get queued immediately - the scheduler will handle them
        log.info("강제 예약 알림 생성 완료: notificationId={}, scheduledAt={}",
                notification.getNotificationId(), notification.getScheduledAt());

        return notification;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getAllNotifications(Pageable pageable) {
        log.debug("백오피스 전체 알림 조회 요청: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<Notification> notifications = notificationRepository.findAllNotifications(pageable);

        log.info("백오피스 전체 알림 조회 완료: totalElements={}, totalPages={}, currentPage={}",
                notifications.getTotalElements(), notifications.getTotalPages(), notifications.getNumber());

        return notifications;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> searchNotifications(NotificationSearchCriteria criteria, Pageable pageable) {
        log.info("백오피스 알림 검색 요청: criteria={}, page={}, size={}",
                criteria, pageable.getPageNumber(), pageable.getPageSize());

        Page<Notification> notifications = notificationRepository.searchNotifications(criteria, pageable);

        log.info("백오피스 알림 검색 완료: totalElements={}, totalPages={}, currentPage={}",
                notifications.getTotalElements(), notifications.getTotalPages(), notifications.getNumber());

        return notifications;
    }

    @Override
    @Transactional
    public Notification cancelScheduledNotification(String notificationId) {
        log.info("예약 알림 취소 요청: notificationId={}", notificationId);

        // 알림 조회
        Notification notification = notificationRepository.findNotificationById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));

        // 상태 검증 - SCHEDULED 상태만 취소 가능
        if (notification.getStatus() != NotificationStatus.SCHEDULED) {
            throw new IllegalStateException(
                    String.format("예약 상태의 알림만 취소할 수 있습니다. 현재 상태: %s", notification.getStatus())
            );
        }

        // 상태를 CANCELLED로 변경
        notification.cancel();

        // 변경사항 저장
        notification = notificationRepository.saveNotification(notification);

        log.info("예약 알림 취소 완료: notificationId={}", notificationId);

        return notification;
    }

    @Override
    public String renderEmailPreview(Notification notification) {
        log.info("이메일 미리보기 렌더링: notificationId={}, templateId={}",
                notification.getNotificationId(), notification.getTemplateId());

        // 템플릿 조회
        NotificationTemplate template = templateRepository
                .findTemplateById(notification.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + notification.getTemplateId()));

        Map<String, Object> variables = new HashMap<>();

        // 기본 변수 설정
        variables.put("userId", notification.getUserId() != null ? notification.getUserId() : "User");

        // 템플릿에 따른 변수 설정
        if (template.getTemplateId().contains("querydaily")) {
            // QueryDaily 관련 템플릿
            variables.put("userName", extractUserName(notification.getUserId()));
            variables.put("currentDay", 1);
            variables.put("totalDays", 3);

            // 기본 질문과 힌트 설정
            variables.put("question", "RESTful API 설계 원칙에 대해 설명해주세요.");
            variables.put("hint", "REST의 6가지 제약 조건과 HTTP 메소드의 올바른 사용법을 생각해보세요.");
            variables.put("nextTopic", "데이터베이스 인덱싱");

            if (notification.getContent() != null) {
                // content에서 추가 정보가 있다면 추출 (향후 JSON 파싱으로 개선 가능)
                try {
                    // JSON 파싱 로직 추가 예정
                } catch (Exception e) {
                    log.debug("추가 콘텐츠 파싱 스킵: {}", e.getMessage());
                }
            }
        }

        // Thymeleaf를 사용한 템플릿 렌더링
        try {
            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariables(variables);

            return templateEngine.process(template.getTemplateId(), context);
        } catch (Exception e) {
            log.error("템플릿 렌더링 실패: {}", e.getMessage());
            // 실패 시 기본 콘텐츠 반환
            return notification.getContent() != null ? notification.getContent() :
                   "<p>미리보기를 생성할 수 없습니다.</p>";
        }
    }

    private String extractUserName(String userId) {
        if (userId == null || !userId.contains("@")) {
            return "사용자";
        }
        return userId.split("@")[0];
    }
}
