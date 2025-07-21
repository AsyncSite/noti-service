package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.dto.NotificationResponse;
import com.asyncsite.notiservice.adapter.in.dto.SendNotificationRequest;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.port.in.GetNotificationUseCase;
import com.asyncsite.notiservice.domain.port.in.SendNotificationUseCase;
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

    private final SendNotificationUseCase sendNotificationUseCase;
    private final GetNotificationUseCase getNotificationUseCase;

    @PostMapping
    public CompletableFuture<ResponseEntity<NotificationResponse>> sendNotification(
            @RequestBody SendNotificationRequest request) {

        log.info("알림 발송 요청: userId={}, eventType={}", request.getUserId(), request.getEventType());

        return sendNotificationUseCase.sendNotification(
                request.getUserId(),
                request.getEventType(),
                request.getMetadata())
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

        return getNotificationUseCase.getNotificationById(notificationId)
                .map(notification -> {
                    NotificationResponse response = NotificationResponse.from(notification);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("사용자 알림 목록 조회: userId={}, page={}, size={}", userId, page, size);

        List<Notification> notifications = getNotificationUseCase.getNotificationsByUserId(userId, page, size);
        List<NotificationResponse> responses = notifications.stream()
                .map(NotificationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{notificationId}/retry")
    public CompletableFuture<ResponseEntity<NotificationResponse>> retryNotification(
            @PathVariable String notificationId) {

        log.info("알림 재시도 요청: notificationId={}", notificationId);

        return sendNotificationUseCase.retryNotification(notificationId)
                .thenApply(notification -> {
                    if (notification != null) {
                        NotificationResponse response = NotificationResponse.from(notification);
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                });
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
