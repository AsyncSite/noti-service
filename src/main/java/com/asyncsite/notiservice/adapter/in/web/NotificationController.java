package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.dto.NotificationResponse;
import com.asyncsite.notiservice.adapter.in.dto.SendNotificationRequest;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationUseCase notificationUseCase;

    @PostMapping
    public CompletableFuture<ResponseEntity<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {

        log.info("알림 발송 요청: userId={}, channelType={}, eventType={}", request.userId(), request.channelType(), request.eventType());

        return notificationUseCase.sendNotification(
                request.userId(),
                ChannelType.valueOf(request.channelType()),
                EventType.valueOf(request.eventType()),
                request.metadata(),
                        request.recipientContact())
                .thenApply(notification -> {
                    if (notification != null) {
                        NotificationResponse response = NotificationResponse.from(notification);
                        return ResponseEntity.status(HttpStatus.CREATED).body(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                });
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable String notificationId) {
        log.info("알림 조회 요청: notificationId={}", notificationId);

        return notificationUseCase.getNotificationById(notificationId)
                .map(notification -> {
                    NotificationResponse response = NotificationResponse.from(notification);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "EMAIL") String channelType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("사용자 알림 목록 조회: userId={}, channelType={} page={}, size={}", userId, channelType, page, size);

        List<Notification> notifications = notificationUseCase.getNotificationsByUserId(userId, ChannelType.valueOf(channelType), page, size);
        List<NotificationResponse> responses = notifications.stream()
                .map(NotificationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{notificationId}/retry")
    public CompletableFuture<ResponseEntity<NotificationResponse>> retryNotification(
            @PathVariable String notificationId) {

        log.info("알림 재시도 요청: notificationId={}", notificationId);

        return notificationUseCase.retryNotification(notificationId)
                .thenApply(notification -> {
                    if (notification != null) {
                        NotificationResponse response = NotificationResponse.from(notification);
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                });
    }

}
