package com.asyncsite.notiservice.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationChannelTest {
    @Test
    void createChannel() {
        NotificationChannel channel = NotificationChannel.builder()
                .channelId("1L")
                .notificationId("1L")
                .channelType(NotificationChannel.ChannelType.EMAIL)
                .recipient("user@email.com")
                .status(NotificationChannel.Status.PENDING)
                .sentAt(null)
                .externalId(null)
                .responseData(Map.of())
                .errorMessage(null)
                .retryCount(0)
                .lastRetryAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assertThat(channel).isNotNull();
        assertThat(channel.getStatus()).isEqualTo(NotificationChannel.Status.PENDING);
    }

    @Test
    void changeStatus() {
        NotificationChannel channel = NotificationChannel.builder()
                .channelId("1L")
                .notificationId("1L")
                .channelType(NotificationChannel.ChannelType.EMAIL)
                .recipient("user@email.com")
                .status(NotificationChannel.Status.PENDING)
                .sentAt(null)
                .externalId(null)
                .responseData(Map.of())
                .errorMessage(null)
                .retryCount(0)
                .lastRetryAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        NotificationChannel sent = channel.withStatus(NotificationChannel.Status.SENT);
        assertThat(sent.getStatus()).isEqualTo(NotificationChannel.Status.SENT);
    }

    @Test
    void retryLogic() {
        NotificationChannel channel = NotificationChannel.builder()
                .channelId("1L")
                .notificationId("1L")
                .channelType(NotificationChannel.ChannelType.EMAIL)
                .recipient("user@email.com")
                .status(NotificationChannel.Status.FAILED)
                .sentAt(null)
                .externalId(null)
                .responseData(Map.of())
                .errorMessage("에러")
                .retryCount(1)
                .lastRetryAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assertThat(channel.canRetry()).isTrue();
    }

    @Test
    void errorMessageUpdate() {
        NotificationChannel channel = NotificationChannel.builder()
                .channelId("1L")
                .notificationId("1L")
                .channelType(NotificationChannel.ChannelType.EMAIL)
                .recipient("user@email.com")
                .status(NotificationChannel.Status.FAILED)
                .sentAt(null)
                .externalId(null)
                .responseData(Map.of())
                .errorMessage(null)
                .retryCount(1)
                .lastRetryAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        NotificationChannel withError = channel.withErrorMessage("실패");
        assertThat(withError.getErrorMessage()).isEqualTo("실패");
    }
} 