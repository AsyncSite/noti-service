package com.asyncsite.notiservice.domain.model;

import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Notification 도메인 모델 테스트")
class NotificationTest {

    @Test
    @DisplayName("새로운 알림을 생성할 수 있다")
    void createNotification() {
        // given
        String userId = "user123";
        String templateId = "template123";
        ChannelType channelType = ChannelType.EMAIL;
        String title = "Test Title";
        String content = "Test Content";
        String recipientContact = "test@example.com";

        // when
        Notification notification = Notification.create(
                userId, templateId, channelType, title, content, recipientContact
        );

        // then
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getTemplateId()).isEqualTo(templateId);
        assertThat(notification.getChannelType()).isEqualTo(channelType);
        assertThat(notification.getTitle()).isEqualTo(title);
        assertThat(notification.getContent()).isEqualTo(content);
        assertThat(notification.getRecipientContact()).isEqualTo(recipientContact);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRetryCount()).isEqualTo(0);
        assertThat(notification.getVersion()).isEqualTo(0L);
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isNotNull();
        assertThat(notification.getSentAt()).isNull();
    }

    @Test
    @DisplayName("알림을 발송 완료로 표시할 수 있다")
    void markAsSent() {
        // given
        Notification notification = createTestNotification();

        // when
        notification.markAsSent();

        // then
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("알림을 실패로 표시할 수 있다")
    void fail() {
        // given
        Notification notification = createTestNotification();

        // when
        notification.fail();

        // then
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("재시도 가능한 알림은 재시도할 수 있다")
    void prepareRetry() {
        // given
        Notification notification = createTestNotification();
        notification.fail(); // 실패 상태로 변경

        // when
        notification.prepareRetry();

        // then
        assertThat(notification.getRetryCount()).isEqualTo(1);
        assertThat(notification.getUpdatedAt()).isNotNull();
        assertThat(notification.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("재시도 횟수가 초과된 알림은 재시도할 수 없다")
    void cannotRetryWhenRetryCountExceeded() {
        // given
        Notification notification = Notification.builder()
                .userId("user123")
                .templateId("template123")
                .channelType(ChannelType.EMAIL)
                .title("Test Title")
                .content("Test Content")
                .recipientContact("test@example.com")
                .status(NotificationStatus.FAILED)
                .retryCount(3)
                .build();

        // when & then
        assertThatThrownBy(notification::prepareRetry)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재시도할 수 없는 알림입니다");
    }

    @Test
    @DisplayName("알림 상태를 올바르게 확인할 수 있다")
    void checkNotificationStatus() {
        // given
        Notification pendingNotification = createTestNotification();
        Notification sentNotification = createTestNotification();
        sentNotification.markAsSent();
        Notification failedNotification = createTestNotification();
        failedNotification.fail();

        // then
        assertThat(pendingNotification.isPending()).isTrue();
        assertThat(pendingNotification.isProcessing()).isTrue();
        assertThat(pendingNotification.isCompleted()).isFalse();

        assertThat(sentNotification.isSent()).isTrue();
        assertThat(sentNotification.isCompleted()).isTrue();
        assertThat(sentNotification.isProcessing()).isFalse();

        assertThat(failedNotification.isFailed()).isTrue();
        assertThat(failedNotification.canRetry()).isTrue();
        assertThat(failedNotification.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("재시도 횟수 초과시 완료된 상태로 판단된다")
    void isCompletedWhenRetryCountExceeded() {
        // given
        Notification notification = Notification.builder()
                .userId("user123")
                .templateId("template123")
                .channelType(ChannelType.EMAIL)
                .title("Test Title")
                .content("Test Content")
                .recipientContact("test@example.com")
                .status(NotificationStatus.FAILED)
                .retryCount(3)
                .build();

        // then
        assertThat(notification.canRetry()).isFalse();
        assertThat(notification.isCompleted()).isTrue();
    }

    private Notification createTestNotification() {
        return Notification.create(
                "user123",
                "template123",
                ChannelType.EMAIL,
                "Test Title",
                "Test Content",
                "test@example.com"
        );
    }
} 