package com.asyncsite.notiservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationChannel 도메인 모델 테스트")
class NotificationChannelTest {

    @Nested
    @DisplayName("채널 생성 테스트")
    class CreateChannelTests {

        @Test
        @DisplayName("일반 채널 생성 - 성공")
        void createChannel_Success() {
            // when
            NotificationChannel channel = NotificationChannel.create(
                    "notification-1",
                    NotificationChannel.ChannelType.EMAIL,
                    "user@example.com"
            );

            // then
            assertThat(channel).isNotNull();
            assertThat(channel.getNotificationId()).isEqualTo("notification-1");
            assertThat(channel.getChannelType()).isEqualTo(NotificationChannel.ChannelType.EMAIL);
            assertThat(channel.getRecipient()).isEqualTo("user@example.com");
            assertThat(channel.getStatus()).isEqualTo(NotificationChannel.Status.PENDING);
            assertThat(channel.getRetryCount()).isEqualTo(0);
            assertThat(channel.getVersion()).isEqualTo(0L);
            assertThat(channel.getCreatedAt()).isNotNull();
            assertThat(channel.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("이메일 채널 생성")
        void createEmailChannel_Success() {
            // when
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1",
                    "user@example.com"
            );

            // then
            assertThat(channel.getChannelType()).isEqualTo(NotificationChannel.ChannelType.EMAIL);
            assertThat(channel.getRecipient()).isEqualTo("user@example.com");
            assertThat(channel.getStatus()).isEqualTo(NotificationChannel.Status.PENDING);
        }

        @Test
        @DisplayName("Discord 채널 생성")
        void createDiscordChannel_Success() {
            // when
            NotificationChannel channel = NotificationChannel.createDiscordChannel(
                    "notification-1",
                    "https://discord.com/webhook/123"
            );

            // then
            assertThat(channel.getChannelType()).isEqualTo(NotificationChannel.ChannelType.DISCORD);
            assertThat(channel.getRecipient()).isEqualTo("https://discord.com/webhook/123");
            assertThat(channel.getStatus()).isEqualTo(NotificationChannel.Status.PENDING);
        }

        @Test
        @DisplayName("Push 채널 생성")
        void createPushChannel_Success() {
            // when
            NotificationChannel channel = NotificationChannel.createPushChannel(
                    "notification-1",
                    "push-token-123"
            );

            // then
            assertThat(channel.getChannelType()).isEqualTo(NotificationChannel.ChannelType.PUSH);
            assertThat(channel.getRecipient()).isEqualTo("push-token-123");
            assertThat(channel.getStatus()).isEqualTo(NotificationChannel.Status.PENDING);
        }
    }

    @Nested
    @DisplayName("채널 상태 변경 테스트")
    class StatusChangeTests {

        @Test
        @DisplayName("채널 발송 성공 처리")
        void markAsSent_Success() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );
            String externalId = "msg-123";
            Map<String, Object> responseData = Map.of("messageId", "msg-123", "status", "delivered");

            // when
            NotificationChannel sentChannel = channel.markAsSent(externalId, responseData);

            // then
            assertThat(sentChannel.getStatus()).isEqualTo(NotificationChannel.Status.SENT);
            assertThat(sentChannel.getExternalId()).isEqualTo("msg-123");
            assertThat(sentChannel.getResponseData()).containsEntry("messageId", "msg-123");
            assertThat(sentChannel.getSentAt()).isNotNull();
            assertThat(sentChannel.getVersion()).isEqualTo(1L);
            assertThat(sentChannel.getUpdatedAt()).isAfter(channel.getUpdatedAt());
        }

        @Test
        @DisplayName("채널 발송 실패 처리")
        void markAsFailed_Success() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );

            // when
            NotificationChannel failedChannel = channel.markAsFailed("SMTP 연결 실패");

            // then
            assertThat(failedChannel.getStatus()).isEqualTo(NotificationChannel.Status.FAILED);
            assertThat(failedChannel.getErrorMessage()).isEqualTo("SMTP 연결 실패");
            assertThat(failedChannel.getVersion()).isEqualTo(1L);
            assertThat(failedChannel.getUpdatedAt()).isAfter(channel.getUpdatedAt());
        }

        @Test
        @DisplayName("채널 재시도 준비")
        void prepareRetry_Success() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            ).markAsFailed("네트워크 오류");

            // when
            NotificationChannel retryChannel = channel.prepareRetry();

            // then
            assertThat(retryChannel.getStatus()).isEqualTo(NotificationChannel.Status.RETRY);
            assertThat(retryChannel.getRetryCount()).isEqualTo(1);
            assertThat(retryChannel.getErrorMessage()).isNull();
            assertThat(retryChannel.getLastRetryAt()).isNotNull();
            assertThat(retryChannel.getVersion()).isEqualTo(2L); // 실패 처리 후 재시도이므로 2
        }
    }

    @Nested
    @DisplayName("채널 정보 업데이트 테스트")
    class UpdateTests {

        @Test
        @DisplayName("응답 데이터 업데이트")
        void updateResponseData_Success() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );
            Map<String, Object> newResponseData = Map.of("deliveryTime", "2023-01-01T10:00:00");

            // when
            NotificationChannel updatedChannel = channel.updateResponseData(newResponseData);

            // then
            assertThat(updatedChannel.getResponseData()).containsEntry("deliveryTime", "2023-01-01T10:00:00");
            assertThat(updatedChannel.getVersion()).isEqualTo(1L);
            assertThat(updatedChannel.getUpdatedAt()).isAfter(channel.getUpdatedAt());
        }

        @Test
        @DisplayName("수신자 정보 업데이트")
        void updateRecipient_Success() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "old@example.com"
            );

            // when
            NotificationChannel updatedChannel = channel.updateRecipient("new@example.com");

            // then
            assertThat(updatedChannel.getRecipient()).isEqualTo("new@example.com");
            assertThat(updatedChannel.getVersion()).isEqualTo(1L);
            assertThat(updatedChannel.getUpdatedAt()).isAfter(channel.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("채널 상태 확인 테스트")
    class StatusCheckTests {

        @Test
        @DisplayName("발송 성공 여부 확인")
        void isSent_Success() {
            // given
            NotificationChannel pendingChannel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );
            NotificationChannel sentChannel = pendingChannel.markAsSent("msg-123", Map.of());

            // then
            assertThat(pendingChannel.isSent()).isFalse();
            assertThat(sentChannel.isSent()).isTrue();
        }

        @Test
        @DisplayName("재시도 가능 여부 확인")
        void canRetry_Success() {
            // given
            NotificationChannel pendingChannel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );
            NotificationChannel failedChannel = pendingChannel.markAsFailed("오류");
            NotificationChannel sentChannel = pendingChannel.markAsSent("msg-123", Map.of());

            // then
            assertThat(pendingChannel.canRetry()).isFalse(); // PENDING 상태는 재시도 불가
            assertThat(failedChannel.canRetry()).isTrue();   // FAILED 상태는 재시도 가능
            assertThat(sentChannel.canRetry()).isFalse();    // SENT 상태는 재시도 불가
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 검증 테스트")
    class BusinessRuleTests {

        @Test
        @DisplayName("최대 재시도 횟수 도달 시 예외 발생")
        void maxRetryCountReached() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );

            // 최대 재시도 횟수에 도달할 때까지 재시도
            for (int i = 0; i < 3; i++) {
                channel = channel.markAsFailed("재시도 실패 " + i);
                channel = channel.prepareRetry();
            }

            // 마지막 실패 후 재시도 시도
            channel = channel.markAsFailed("최종 실패");

            // when & then
            final NotificationChannel finalChannel = channel;
            assertThatThrownBy(finalChannel::prepareRetry)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재시도할 수 없는 채널입니다");
        }

        @Test
        @DisplayName("발송 완료된 채널은 재시도할 수 없음")
        void cannotRetry_WhenSent() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            ).markAsSent("msg-123", Map.of());

            // when & then
            assertThatThrownBy(channel::prepareRetry)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재시도할 수 없는 채널입니다");
        }

        @Test
        @DisplayName("PENDING 상태에서는 재시도할 수 없음")
        void cannotRetry_WhenPending() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );

            // when & then
            assertThatThrownBy(channel::prepareRetry)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재시도할 수 없는 채널입니다");
        }

        @Test
        @DisplayName("RETRY 상태에서는 재시도할 수 없음")
        void cannotRetry_WhenRetry() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            ).markAsFailed("발송 실패").prepareRetry();

            // when & then
            assertThatThrownBy(channel::prepareRetry)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재시도할 수 없는 채널입니다");
        }
    }

    @Nested
    @DisplayName("채널 상태 확인 상세 테스트")
    class DetailedStatusCheckTests {

        @Test
        @DisplayName("모든 상태 확인 메서드 테스트")
        void allStatusCheckMethods() {
            // given
            NotificationChannel pending = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );
            NotificationChannel sent = pending.markAsSent("msg-123", Map.of());
            NotificationChannel failed = pending.markAsFailed("발송 실패");
            NotificationChannel retry = failed.prepareRetry();

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
        @DisplayName("완료 상태 확인")
        void isCompleted_DetailedTest() {
            // given
            NotificationChannel pending = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );
            NotificationChannel sent = pending.markAsSent("msg-123", Map.of());
            NotificationChannel failed = pending.markAsFailed("발송 실패");
            NotificationChannel retry = failed.prepareRetry();

            // 최대 재시도 횟수 도달
            NotificationChannel maxRetried = failed;
            for (int i = 0; i < 3; i++) {
                maxRetried = maxRetried.prepareRetry().markAsFailed("재시도 실패");
            }

            // then
            assertThat(pending.isCompleted()).isFalse();     // PENDING - 처리 중
            assertThat(sent.isCompleted()).isTrue();         // SENT - 완료
            assertThat(failed.isCompleted()).isFalse();      // FAILED - 재시도 가능하므로 미완료
            assertThat(retry.isCompleted()).isFalse();       // RETRY - 처리 중
            assertThat(maxRetried.isCompleted()).isTrue();   // 최대 재시도 도달 - 완료
        }

        @Test
        @DisplayName("처리 중 상태 확인")
        void isProcessing_DetailedTest() {
            // given
            NotificationChannel pending = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );
            NotificationChannel sent = pending.markAsSent("msg-123", Map.of());
            NotificationChannel failed = pending.markAsFailed("발송 실패");
            NotificationChannel retry = failed.prepareRetry();

            // then
            assertThat(pending.isProcessing()).isTrue();   // PENDING - 처리 중
            assertThat(sent.isProcessing()).isFalse();     // SENT - 처리 완료
            assertThat(failed.isProcessing()).isFalse();   // FAILED - 처리 실패
            assertThat(retry.isProcessing()).isTrue();     // RETRY - 재시도 중
        }

        @Test
        @DisplayName("채널 타입 확인")
        void isChannelType_Success() {
            // given
            NotificationChannel emailChannel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );
            NotificationChannel discordChannel = NotificationChannel.createDiscordChannel(
                    "notification-1", "https://discord.com/webhook/123"
            );
            NotificationChannel pushChannel = NotificationChannel.createPushChannel(
                    "notification-1", "push-token-123"
            );

            // then
            assertThat(emailChannel.isChannelType(NotificationChannel.ChannelType.EMAIL)).isTrue();
            assertThat(emailChannel.isChannelType(NotificationChannel.ChannelType.DISCORD)).isFalse();
            assertThat(emailChannel.isChannelType(NotificationChannel.ChannelType.PUSH)).isFalse();

            assertThat(discordChannel.isChannelType(NotificationChannel.ChannelType.EMAIL)).isFalse();
            assertThat(discordChannel.isChannelType(NotificationChannel.ChannelType.DISCORD)).isTrue();
            assertThat(discordChannel.isChannelType(NotificationChannel.ChannelType.PUSH)).isFalse();

            assertThat(pushChannel.isChannelType(NotificationChannel.ChannelType.EMAIL)).isFalse();
            assertThat(pushChannel.isChannelType(NotificationChannel.ChannelType.DISCORD)).isFalse();
            assertThat(pushChannel.isChannelType(NotificationChannel.ChannelType.PUSH)).isTrue();
        }

        @Test
        @DisplayName("최대 재시도 횟수 도달 확인")
        void hasReachedMaxRetries_Success() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );

            // when & then - 초기 상태
            assertThat(channel.hasReachedMaxRetries()).isFalse();

            // when & then - 재시도 횟수 증가
            for (int i = 1; i <= 2; i++) {
                channel = channel.markAsFailed("실패 " + i).prepareRetry();
                assertThat(channel.hasReachedMaxRetries()).isFalse();
                assertThat(channel.getRetryCount()).isEqualTo(i);
            }

            // when & then - 최대 재시도 도달
            channel = channel.markAsFailed("최종 실패").prepareRetry();
            assertThat(channel.hasReachedMaxRetries()).isTrue();
            assertThat(channel.getRetryCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("마지막 시도 시간 확인")
        void getLastAttemptTime_Success() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );

            // when & then - 초기 상태 (lastRetryAt이 없으므로 createdAt 반환)
            assertThat(channel.getLastAttemptTime()).isEqualTo(channel.getCreatedAt());

            // when - 재시도 후
            NotificationChannel retried = channel.markAsFailed("발송 실패").prepareRetry();

            // then - lastRetryAt 반환
            assertThat(retried.getLastAttemptTime()).isEqualTo(retried.getLastRetryAt());
            assertThat(retried.getLastAttemptTime()).isAfter(channel.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("null 외부 ID와 응답 데이터로 발송 성공 처리")
        void markAsSent_WithNullValues() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );

            // when
            NotificationChannel sent = channel.markAsSent(null, null);

            // then
            assertThat(sent.isSent()).isTrue();
            assertThat(sent.getExternalId()).isNull();
            assertThat(sent.getResponseData()).isNull();
        }

        @Test
        @DisplayName("빈 문자열 수신자로 채널 생성")
        void createWithEmptyRecipient() {
            // when
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", ""
            );

            // then
            assertThat(channel.getRecipient()).isEmpty();
            assertThat(channel.getChannelType()).isEqualTo(NotificationChannel.ChannelType.EMAIL);
        }

        @Test
        @DisplayName("null 알림 ID로 채널 생성")
        void createWithNullNotificationId() {
            // when
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    null, "user@example.com"
            );

            // then
            assertThat(channel.getNotificationId()).isNull();
            assertThat(channel.getRecipient()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("연속 상태 변경")
        void consecutiveStatusChanges() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );

            // when - 연속으로 상태 변경
            NotificationChannel result = channel
                    .markAsFailed("첫 번째 실패")
                    .prepareRetry()
                    .markAsFailed("두 번째 실패")
                    .prepareRetry()
                    .markAsSent("msg-123", Map.of("deliveryId", "final-123"));

            // then
            assertThat(result.getStatus()).isEqualTo(NotificationChannel.Status.SENT);
            assertThat(result.getRetryCount()).isEqualTo(2);
            assertThat(result.getExternalId()).isEqualTo("msg-123");
            assertThat(result.getVersion()).isEqualTo(5L); // 5번의 상태 변경
            assertThat(result.getLastRetryAt()).isNotNull();
        }

        @Test
        @DisplayName("복잡한 응답 데이터 처리")
        void complexResponseData() {
            // given
            NotificationChannel channel = NotificationChannel.createEmailChannel(
                    "notification-1", "user@example.com"
            );
            Map<String, Object> complexData = Map.of(
                "messageId", "msg-123",
                "deliveryTime", "2023-01-01T10:00:00",
                "provider", "SendGrid",
                "metadata", Map.of(
                    "campaignId", "camp-456",
                    "tags", java.util.List.of("notification", "study", "approval")
                )
            );

            // when
            NotificationChannel sent = channel.markAsSent("external-123", complexData);

            // then
            assertThat(sent.getResponseData()).containsEntry("messageId", "msg-123");
            assertThat(sent.getResponseData()).containsEntry("provider", "SendGrid");
            Map<String, Object> metadata = (Map<String, Object>) sent.getResponseData().get("metadata");
            assertThat(metadata).containsEntry("campaignId", "camp-456");
        }
    }
} 