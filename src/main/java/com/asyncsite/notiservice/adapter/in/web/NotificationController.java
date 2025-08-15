package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.dto.ApiResponse;
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
import java.util.Map;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/noti")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationUseCase notificationUseCase;

    @PostMapping
    public CompletableFuture<ResponseEntity<ApiResponse<NotificationResponse>>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {

        log.info("알림 발송 요청: userId={}, channelType={}, templateId={}", request.userId(), request.channelType(), request.templateId());

        Map<String, Object> meta = new java.util.HashMap<>();
        if (request.templateId() != null && !request.templateId().isBlank()) {
            meta.put("templateId", request.templateId());
        }
        meta.put("variables", request.variables() == null ? java.util.Map.of() : request.variables());

        return notificationUseCase.sendNotification(
                        request.userId(),
                        ChannelType.valueOf(request.channelType()),
                        EventType.valueOf(request.eventType()),
                        meta,
                        request.recipientContact())
                .thenApply(notification -> {
                    if (notification != null) {
                        NotificationResponse response = NotificationResponse.from(notification);
                        return ApiResponse.success(response);
                    } else {
                        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "알림 발송 오류");
                    }
                });
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @PathVariable String notificationId) {
        log.info("알림 조회 요청: notificationId={}", notificationId);

        return notificationUseCase.getNotificationById(notificationId)
                .map(notification -> {
                    NotificationResponse response = NotificationResponse.from(notification);
                    return ApiResponse.success(response);
                }).get();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "EMAIL") String channelType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("사용자 알림 목록 조회: userId={}, channelType={} page={}, size={}", userId, channelType, page, size);

        List<Notification> notifications = notificationUseCase.getNotificationsByUserId(userId, ChannelType.valueOf(channelType), page, size);
        List<NotificationResponse> responses = notifications.stream()
                .map(NotificationResponse::from)
                .toList();

        return ApiResponse.success(responses);
    }

    @PatchMapping("/{notificationId}/retry")
    public CompletableFuture<ResponseEntity<ApiResponse<NotificationResponse>>> retryNotification(
            @PathVariable String notificationId) {

        log.info("알림 재시도 요청: notificationId={}", notificationId);

        return notificationUseCase.retryNotification(notificationId)
                .thenApply(notification -> {
                    if (notification != null) {
                        NotificationResponse response = NotificationResponse.from(notification);
                        return ApiResponse.success(response);
                    } else {
                        return ApiResponse.error("", "error");
                    }
                });
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
