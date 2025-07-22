package com.asyncsite.notiservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Notification 도메인 모델 테스트")
class NotificationTest {

    @Nested
    @DisplayName("알림 생성 테스트")
    class CreateNotificationTests {

        @Test
        @DisplayName("기본 알림 생성 - 성공")
        void createNotification_Success() {
            // when
            Notification notification = Notification.create(
                    "user-1",
                    "STUDY_APPROVAL",
                    "스터디 승인",
                    "스터디가 승인되었습니다.",
                    Map.of("studyId", "123")
            );

            // then
            assertThat(notification).isNotNull();
            assertThat(notification.getUserId()).isEqualTo("user-1");
            assertThat(notification.getEventType()).isEqualTo("STUDY_APPROVAL");
            assertThat(notification.getTitle()).isEqualTo("스터디 승인");
            assertThat(notification.getContent()).isEqualTo("스터디가 승인되었습니다.");
            assertThat(notification.getMetadata()).containsEntry("studyId", "123");
            assertThat(notification.getStatus()).isEqualTo(Notification.NotificationStatus.PENDING);
            assertThat(notification.getRetryCount()).isEqualTo(0);
            assertThat(notification.getVersion()).isEqualTo(0L);
            assertThat(notification.getCreatedAt()).isNotNull();
            assertThat(notification.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING 상태 알림 생성")
        void createPendingNotification() {
            // when
            Notification notification = Notification.createPending(
                    "user-1",
                    "MARKETING_EMAIL",
                    Map.of("campaignId", "123")
            );

            // then
            assertThat(notification.getStatus()).isEqualTo(Notification.NotificationStatus.PENDING);
            assertThat(notification.getVersion()).isEqualTo(0L);
            assertThat(notification.getTitle()).isEqualTo("알림");
            assertThat(notification.getContent()).isEqualTo("알림 내용");
        }

        @Test
        @DisplayName("FAILED 상태 알림 생성")
        void createFailedNotification() {
            // when
            Notification notification = Notification.createFailed(
                    "user-1",
                    "COMMENT_REPLY",
                    Map.of("commentId", "456"),
                    "발송 실패"
            );

            // then
            assertThat(notification.getStatus()).isEqualTo(Notification.NotificationStatus.FAILED);
            assertThat(notification.getErrorMessage()).isEqualTo("발송 실패");
            assertThat(notification.getVersion()).isEqualTo(0L);
        }

        @Test
        @DisplayName("DISABLED 상태 알림 생성")
        void createDisabledNotification() {
            // when
            Notification notification = Notification.createDisabled(
                    "user-1",
                    "DISABLED_EVENT",
                    Map.of()
            );

            // then
            assertThat(notification.getStatus()).isEqualTo(Notification.NotificationStatus.SENT); // 비활성화는 SENT로 처리
            assertThat(notification.getVersion()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("알림 상태 변경 테스트")
    class StatusChangeTests {

        @Test
        @DisplayName("알림 발송 완료 처리")
        void markAsSent_Success() {
            // given
            Notification notification = Notification.createPending(
                    "user-1", "TEST_EVENT", Map.of()
            );

            // when
            Notification sentNotification = notification.markAsSent();

            // then
            assertThat(sentNotification.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
            assertThat(sentNotification.getSentAt()).isNotNull();
            assertThat(sentNotification.getVersion()).isEqualTo(1L);
            assertThat(sentNotification.getUpdatedAt()).isAfter(notification.getUpdatedAt());
        }

        @Test
        @DisplayName("알림 발송 실패 처리")
        void markAsFailed_Success() {
            // given
            Notification notification = Notification.createPending(
                    "user-1", "TEST_EVENT", Map.of()
            );

            // when
            Notification failedNotification = notification.markAsFailed("SMTP 연결 실패");

            // then
            assertThat(failedNotification.getStatus()).isEqualTo(Notification.NotificationStatus.FAILED);
            assertThat(failedNotification.getErrorMessage()).isEqualTo("SMTP 연결 실패");
            assertThat(failedNotification.getVersion()).isEqualTo(1L);
            assertThat(failedNotification.getUpdatedAt()).isAfter(notification.getUpdatedAt());
        }

        @Test
        @DisplayName("재시도 준비")
        void prepareRetry_Success() {
            // given
            Notification notification = Notification.createFailed(
                    "user-1", "TEST_EVENT", Map.of(), "발송 실패"
            );

            // when
            Notification retryNotification = notification.prepareRetry();

            // then
            assertThat(retryNotification.getStatus()).isEqualTo(Notification.NotificationStatus.RETRY);
            assertThat(retryNotification.getRetryCount()).isEqualTo(1);
            assertThat(retryNotification.getErrorMessage()).isEqualTo("발송 실패"); // 에러 메시지는 유지됨
            assertThat(retryNotification.getVersion()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("알림 내용 수정 테스트")
    class ContentUpdateTests {

        @Test
        @DisplayName("알림 내용 업데이트")
        void updateContent_Success() {
            // given
            Notification notification = Notification.create(
                    "user-1", "TEST_EVENT", "원본 제목", "원본 내용", Map.of()
            );

            // when
            Notification updatedNotification = notification.updateContent(
                    "수정된 제목",
                    "수정된 내용"
            );

            // then
            assertThat(updatedNotification.getTitle()).isEqualTo("수정된 제목");
            assertThat(updatedNotification.getContent()).isEqualTo("수정된 내용");
            assertThat(updatedNotification.getVersion()).isEqualTo(1L);
            assertThat(updatedNotification.getUpdatedAt()).isAfter(notification.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("채널 관리 테스트")
    class ChannelManagementTests {

        @Test
        @DisplayName("채널 추가")
        void addChannels_Success() {
            // given
            Notification notification = Notification.create(
                    "user-1", "TEST_EVENT", "테스트", "내용", Map.of()
            );

            NotificationChannel emailChannel = NotificationChannel.createEmailChannel(
                    notification.getNotificationId(), "user@example.com"
            );
            NotificationChannel discordChannel = NotificationChannel.createDiscordChannel(
                    notification.getNotificationId(), "https://discord.com/webhook"
            );

            // when
            Notification updatedNotification = notification.addChannels(
                    java.util.List.of(emailChannel, discordChannel)
            );

            // then
            assertThat(updatedNotification.getChannels()).hasSize(2);
            assertThat(updatedNotification.getChannels()).contains(emailChannel, discordChannel);
            assertThat(updatedNotification.getVersion()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 검증 테스트")
    class BusinessRuleTests {

        @Test
        @DisplayName("최대 재시도 횟수 도달 시 예외 발생")
        void maxRetryCountReached() {
            // given
            Notification notification = Notification.createFailed(
                    "user-1", "TEST_EVENT", Map.of(), "발송 실패"
            );

            // 최대 재시도 횟수에 도달할 때까지 재시도
            for (int i = 0; i < 3; i++) {
                notification = notification.prepareRetry();
                notification = notification.markAsFailed("재시도 실패");
            }

            // when & then
            final Notification finalNotification = notification;
            assertThatThrownBy(finalNotification::prepareRetry)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재시도할 수 없는 알림입니다");
        }

        @Test
        @DisplayName("재시도 불가능한 상태에서 예외 발생 - SENT 상태")
        void cannotRetry_WhenSent() {
            // given
            Notification notification = Notification.createPending("user-1", "TEST_EVENT", Map.of())
                    .markAsSent();

            // when & then
            assertThatThrownBy(notification::prepareRetry)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재시도할 수 없는 알림입니다");
        }

        @Test
        @DisplayName("재시도 불가능한 상태에서 예외 발생 - PENDING 상태")
        void cannotRetry_WhenPending() {
            // given
            Notification notification = Notification.createPending("user-1", "TEST_EVENT", Map.of());

            // when & then
            assertThatThrownBy(notification::prepareRetry)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재시도할 수 없는 알림입니다");
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드 테스트")
    class StatusCheckTests {

        @Test
        @DisplayName("알림 상태 확인 메서드들")
        void statusCheckMethods() {
            // given
            Notification pending = Notification.createPending("user-1", "TEST_EVENT", Map.of());
            Notification sent = pending.markAsSent();
            Notification failed = pending.markAsFailed("발송 실패");
            Notification retry = failed.prepareRetry();

            // then - isPending
            assertThat(pending.isPending()).isTrue();
            assertThat(sent.isPending()).isFalse();
            assertThat(failed.isPending()).isFalse();
            assertThat(retry.isPending()).isFalse();

            // then - isSent
            assertThat(pending.isSent()).isFalse();
            assertThat(sent.isSent()).isTrue();
            assertThat(failed.isSent()).isFalse();
            assertThat(retry.isSent()).isFalse();

            // then - isFailed
            assertThat(pending.isFailed()).isFalse();
            assertThat(sent.isFailed()).isFalse();
            assertThat(failed.isFailed()).isTrue();
            assertThat(retry.isFailed()).isFalse();

            // then - isRetry
            assertThat(pending.isRetry()).isFalse();
            assertThat(sent.isRetry()).isFalse();
            assertThat(failed.isRetry()).isFalse();
            assertThat(retry.isRetry()).isTrue();
        }

        @Test
        @DisplayName("재시도 가능 여부 확인")
        void canRetry_Success() {
            // given
            Notification pending = Notification.createPending("user-1", "TEST_EVENT", Map.of());
            Notification sent = pending.markAsSent();
            Notification failed = pending.markAsFailed("발송 실패");
            Notification retry = failed.prepareRetry();

            // then
            assertThat(pending.canRetry()).isFalse(); // PENDING은 재시도 불가
            assertThat(sent.canRetry()).isFalse();    // SENT는 재시도 불가
            assertThat(failed.canRetry()).isTrue();   // FAILED는 재시도 가능
            assertThat(retry.canRetry()).isFalse();   // RETRY는 재시도 불가 (이미 재시도 중)
        }

        @Test
        @DisplayName("알림 완료 상태 확인")
        void isCompleted_Success() {
            // given
            Notification pending = Notification.createPending("user-1", "TEST_EVENT", Map.of());
            Notification sent = pending.markAsSent();
            Notification failed = pending.markAsFailed("발송 실패");
            Notification retry = failed.prepareRetry();

            // 최대 재시도 횟수 도달
            Notification maxRetried = failed;
            for (int i = 0; i < 3; i++) {
                maxRetried = maxRetried.prepareRetry().markAsFailed("재시도 실패");
            }

            // then
            assertThat(pending.isCompleted()).isFalse();    // PENDING - 처리 중
            assertThat(sent.isCompleted()).isTrue();        // SENT - 완료
            assertThat(failed.isCompleted()).isFalse();     // FAILED - 재시도 가능하므로 미완료
            assertThat(retry.isCompleted()).isFalse();      // RETRY - 처리 중
            assertThat(maxRetried.isCompleted()).isTrue();  // 최대 재시도 도달 - 완료
        }

        @Test
        @DisplayName("알림 처리 중 상태 확인")
        void isProcessing_Success() {
            // given
            Notification pending = Notification.createPending("user-1", "TEST_EVENT", Map.of());
            Notification sent = pending.markAsSent();
            Notification failed = pending.markAsFailed("발송 실패");
            Notification retry = failed.prepareRetry();

            // then
            assertThat(pending.isProcessing()).isTrue();   // PENDING - 처리 중
            assertThat(sent.isProcessing()).isFalse();     // SENT - 처리 완료
            assertThat(failed.isProcessing()).isFalse();   // FAILED - 처리 실패
            assertThat(retry.isProcessing()).isTrue();     // RETRY - 재시도 중
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("null 메타데이터로 알림 생성")
        void createWithNullMetadata() {
            // when & then
            assertThatNoException().isThrownBy(() -> {
                Notification notification = Notification.create(
                        "user-1", "TEST_EVENT", "제목", "내용", null
                );
                assertThat(notification.getMetadata()).isNull();
            });
        }

        @Test
        @DisplayName("빈 문자열로 알림 생성")
        void createWithEmptyStrings() {
            // when
            Notification notification = Notification.create(
                    "", "", "", "", Map.of()
            );

            // then
            assertThat(notification.getUserId()).isEmpty();
            assertThat(notification.getEventType()).isEmpty();
            assertThat(notification.getTitle()).isEmpty();
            assertThat(notification.getContent()).isEmpty();
        }

        @Test
        @DisplayName("연속 상태 변경")
        void consecutiveStatusChanges() {
            // given
            Notification notification = Notification.createPending("user-1", "TEST_EVENT", Map.of());

            // when - 연속으로 상태 변경
            Notification result = notification
                    .markAsFailed("첫 번째 실패")
                    .prepareRetry()
                    .markAsFailed("두 번째 실패")
                    .prepareRetry()
                    .markAsSent();

            // then
            assertThat(result.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
            assertThat(result.getRetryCount()).isEqualTo(2);
            assertThat(result.getVersion()).isEqualTo(5L); // 5번의 상태 변경
        }
    }
} 