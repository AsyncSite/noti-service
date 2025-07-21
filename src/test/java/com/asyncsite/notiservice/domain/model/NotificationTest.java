package com.asyncsite.notiservice.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {
    @Test
    void createNotification() {
        Notification notification = Notification.builder()
                .notificationId("1L")
                .userId("100")
                .eventType("STUDY_UPDATE")
                .title("제목")
                .content("내용")
                .metadata(Map.of("key", "value"))
                .status(Notification.NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .sentAt(null)
                .updatedAt(LocalDateTime.now())
                .retryCount(0)
                .errorMessage(null)
                .channels(List.of())
                .build();
        assertThat(notification).isNotNull();
        assertThat(notification.getStatus()).isEqualTo(Notification.NotificationStatus.PENDING);
    }

    @Test
    void changeStatus() {
        Notification notification = Notification.builder()
                .notificationId("1L")
                .userId("100")
                .eventType("STUDY_UPDATE")
                .title("제목")
                .content("내용")
                .metadata(Map.of())
                .status(Notification.NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .sentAt(null)
                .updatedAt(LocalDateTime.now())
                .retryCount(0)
                .errorMessage(null)
                .channels(List.of())
                .build();
        Notification sent = notification.withStatus(Notification.NotificationStatus.SENT);
        assertThat(sent.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
    }

    @Test
    void retryLogic() {
        Notification notification = Notification.builder()
                .notificationId("1L")
                .userId("100")
                .eventType("STUDY_UPDATE")
                .title("제목")
                .content("내용")
                .metadata(Map.of())
                .status(Notification.NotificationStatus.FAILED)
                .createdAt(LocalDateTime.now())
                .sentAt(null)
                .updatedAt(LocalDateTime.now())
                .retryCount(2)
                .errorMessage("에러")
                .channels(List.of())
                .build();
        assertThat(notification.canRetry()).isTrue();
        Notification retried = notification.withRetryCount(3);
        assertThat(retried.getRetryCount()).isEqualTo(3);
        assertThat(retried.canRetry()).isFalse();
    }

    @Test
    void errorMessageUpdate() {
        Notification notification = Notification.builder()
                .notificationId("1L")
                .userId("100")
                .eventType("STUDY_UPDATE")
                .title("제목")
                .content("내용")
                .metadata(Map.of())
                .status(Notification.NotificationStatus.FAILED)
                .createdAt(LocalDateTime.now())
                .sentAt(null)
                .updatedAt(LocalDateTime.now())
                .retryCount(1)
                .errorMessage(null)
                .channels(List.of())
                .build();
        Notification withError = notification.withErrorMessage("실패");
        assertThat(withError.getErrorMessage()).isEqualTo("실패");
    }
} 