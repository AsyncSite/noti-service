package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.web.dto.ApiResponse;
import com.asyncsite.notiservice.adapter.in.web.dto.NotificationResponse;
import com.asyncsite.notiservice.adapter.in.web.dto.SendNotificationBulkRequest;
import com.asyncsite.notiservice.adapter.in.web.dto.SendNotificationRequest;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/noti")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationUseCase notiUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        log.info("알림 발송 요청: userId={}, channelType={}, templateId={}, scheduledAt={}",
            request.userId(), request.channelType(), request.templateId(), request.scheduledAt());

        Notification notification;
        if (request.scheduledAt() != null) {
            // 예약 발송
            notification = notiUseCase.createScheduledNotification(
                    request.userId(),
                    ChannelType.valueOf(request.channelType()),
                    EventType.valueOf(request.eventType()),
                    request.getMetaData(),
                    request.recipientContact(),
                    request.scheduledAt()
            );
        } else {
            // 즉시 발송
            notification = notiUseCase.createNotification(
                    request.userId(),
                    ChannelType.valueOf(request.channelType()),
                    EventType.valueOf(request.eventType()),
                    request.getMetaData(),
                    request.recipientContact()
            );
        }

        return ApiResponse.success(NotificationResponse.from(notification));
    }

    @PostMapping("/force")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendForceNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        log.info("강제 알림 발송 요청: userId={}, channelType={}, templateId={}",
            request.userId(), request.channelType(), request.templateId());

        Notification notification;
        if (request.scheduledAt() != null) {
            // 예약 발송 (강제)
            notification = notiUseCase.createForceScheduledNotification(
                    request.userId(),
                    ChannelType.valueOf(request.channelType()),
                    EventType.valueOf(request.eventType()),
                    request.getMetaData(),
                    request.recipientContact(),
                    request.scheduledAt()
            );
        } else {
            // 즉시 발송 (강제)
            notification = notiUseCase.createForceNotification(
                    request.userId(),
                    ChannelType.valueOf(request.channelType()),
                    EventType.valueOf(request.eventType()),
                    request.getMetaData(),
                    request.recipientContact()
            );
        }

        return ApiResponse.success(NotificationResponse.from(notification));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotificationBulk(
            @Valid @RequestBody SendNotificationBulkRequest request) {
        log.info("알림 발송 요청: userId={}, channelType={}, templateId={}, scheduledAt={}",
            request.userId(), request.channelType(), request.templateId(), request.scheduledAt());

        Notification res;
        if (request.scheduledAt() != null) {
            // 예약 발송
            res = notiUseCase.createScheduledNotificationBulk(
                    request.userId(),
                    ChannelType.valueOf(request.channelType()),
                    EventType.valueOf(request.eventType()),
                    request.getMetaData(),
                    request.recipientContacts(),
                    request.scheduledAt()
            );
        } else {
            // 즉시 발송
            res = notiUseCase.createNotificationBulk(
                    request.userId(),
                    ChannelType.valueOf(request.channelType()),
                    EventType.valueOf(request.eventType()),
                    request.getMetaData(),
                    request.recipientContacts()
            );
        }
        return ApiResponse.success(NotificationResponse.from(res));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @PathVariable String notificationId) {
        log.info("알림 조회 요청: notificationId={}", notificationId);

        Notification notification = notiUseCase.getNotificationById(notificationId);
        return ApiResponse.success(NotificationResponse.from(notification));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "EMAIL") String channelType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("사용자 알림 목록 조회: userId={}, channelType={} page={}, size={}", userId, channelType, page, size);

        List<Notification> notifications = notiUseCase.getNotificationsByUserId(userId, ChannelType.valueOf(channelType), page, size);
        List<NotificationResponse> responses = notifications.stream()
                .map(NotificationResponse::from)
                .toList();

        return ApiResponse.success(responses);
    }

    @PatchMapping("/{notificationId}/retry")
    public ResponseEntity<ApiResponse<NotificationResponse>> retryNotification(
            @PathVariable String notificationId) throws MessagingException, UnsupportedEncodingException {

        log.info("알림 재시도 요청: notificationId={}", notificationId);

        return ApiResponse.success(NotificationResponse.from(notiUseCase.retryNotification(notificationId)));
    }

    @GetMapping("/event-types")
    public ResponseEntity<ApiResponse<EventType[]>> getEventTypes() {
        return ApiResponse.success(EventType.values());
    }

    @GetMapping("/channel-types")
    public ResponseEntity<ApiResponse<ChannelType[]>> getChannelTypes() {
        return ApiResponse.success(ChannelType.values());
    }
}
