package com.asyncsite.notiservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationSettings 도메인 모델 테스트")
class NotificationSettingsTest {

    @Nested
    @DisplayName("알림 설정 생성 테스트")
    class CreateSettingsTests {

        @Test
        @DisplayName("기본 알림 설정 생성 - 성공")
        void createDefault_Success() {
            // when
            NotificationSettings settings = NotificationSettings.createDefault("user-1");

            // then
            assertThat(settings).isNotNull();
            assertThat(settings.getUserId()).isEqualTo("user-1");
            assertThat(settings.isStudyUpdates()).isTrue();
            assertThat(settings.isMarketing()).isFalse();
            assertThat(settings.isEmailEnabled()).isTrue();
            assertThat(settings.isDiscordEnabled()).isFalse();
            assertThat(settings.isPushEnabled()).isFalse();
            assertThat(settings.getTimezone()).isEqualTo("Asia/Seoul");
            assertThat(settings.getLanguage()).isEqualTo("ko");
            assertThat(settings.getQuietHours()).isNotNull();
            assertThat(settings.getQuietHours().get("enabled")).isEqualTo(false);
            assertThat(settings.getVersion()).isEqualTo(0L);
            assertThat(settings.getCreatedAt()).isNotNull();
            assertThat(settings.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("커스텀 알림 설정 생성 - 성공")
        void create_Success() {
            // given
            Map<String, Object> quietHours = Map.of(
                "enabled", true,
                "startTime", "22:00",
                "endTime", "08:00",
                "weekendsOnly", true
            );

            // when
            NotificationSettings settings = NotificationSettings.create(
                "user-1",
                true,
                true,
                true,
                true,
                true,
                "America/New_York",
                "en",
                quietHours
            );

            // then
            assertThat(settings.getUserId()).isEqualTo("user-1");
            assertThat(settings.isStudyUpdates()).isTrue();
            assertThat(settings.isMarketing()).isTrue();
            assertThat(settings.isEmailEnabled()).isTrue();
            assertThat(settings.isDiscordEnabled()).isTrue();
            assertThat(settings.isPushEnabled()).isTrue();
            assertThat(settings.getTimezone()).isEqualTo("America/New_York");
            assertThat(settings.getLanguage()).isEqualTo("en");
            assertThat(settings.getQuietHours()).containsEntry("enabled", true);
            assertThat(settings.getQuietHours()).containsEntry("weekendsOnly", true);
        }

        @Test
        @DisplayName("null 값으로 설정 생성 시 기본값 사용")
        void create_WithNullValues_UseDefaults() {
            // when
            NotificationSettings settings = NotificationSettings.create(
                "user-1",
                false,
                false,
                false,
                false,
                false,
                null, // timezone null
                null, // language null
                null  // quietHours null
            );

            // then
            assertThat(settings.getTimezone()).isEqualTo("Asia/Seoul");
            assertThat(settings.getLanguage()).isEqualTo("ko");
            assertThat(settings.getQuietHours()).isNotNull();
            assertThat(settings.getQuietHours().get("enabled")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("설정 업데이트 테스트")
    class UpdateSettingsTests {

        private NotificationSettings settings;

        @BeforeEach
        void setUp() {
            settings = NotificationSettings.createDefault("user-1");
        }

        @Test
        @DisplayName("이벤트 설정 업데이트")
        void updateEventSettings_Success() {
            // when
            NotificationSettings updated = settings.updateEventSettings(false, true);

            // then
            assertThat(updated.isStudyUpdates()).isFalse();
            assertThat(updated.isMarketing()).isTrue();
            assertThat(updated.getVersion()).isEqualTo(1L);
            assertThat(updated.getUpdatedAt()).isAfter(settings.getUpdatedAt());
        }

        @Test
        @DisplayName("채널 설정 업데이트")
        void updateChannelSettings_Success() {
            // when
            NotificationSettings updated = settings.updateChannelSettings(false, true, true);

            // then
            assertThat(updated.isEmailEnabled()).isFalse();
            assertThat(updated.isDiscordEnabled()).isTrue();
            assertThat(updated.isPushEnabled()).isTrue();
            assertThat(updated.getVersion()).isEqualTo(1L);
            assertThat(updated.getUpdatedAt()).isAfter(settings.getUpdatedAt());
        }

        @Test
        @DisplayName("특정 채널 활성화/비활성화")
        void updateChannelEnabled_Success() {
            // when
            NotificationSettings emailDisabled = settings.updateChannelEnabled(
                NotificationChannel.ChannelType.EMAIL, false
            );
            NotificationSettings discordEnabled = emailDisabled.updateChannelEnabled(
                NotificationChannel.ChannelType.DISCORD, true
            );

            // then
            assertThat(discordEnabled.isEmailEnabled()).isFalse();
            assertThat(discordEnabled.isDiscordEnabled()).isTrue();
            assertThat(discordEnabled.isPushEnabled()).isFalse(); // 기존 값 유지
            assertThat(discordEnabled.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("지역화 설정 업데이트")
        void updateLocalizationSettings_Success() {
            // when
            NotificationSettings updated = settings.updateLocalizationSettings("Europe/London", "en");

            // then
            assertThat(updated.getTimezone()).isEqualTo("Europe/London");
            assertThat(updated.getLanguage()).isEqualTo("en");
            assertThat(updated.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("지역화 설정 일부만 업데이트")
        void updateLocalizationSettings_Partial() {
            // when
            NotificationSettings timezoneOnly = settings.updateLocalizationSettings("Europe/London", null);
            NotificationSettings languageOnly = settings.updateLocalizationSettings(null, "en");

            // then
            assertThat(timezoneOnly.getTimezone()).isEqualTo("Europe/London");
            assertThat(timezoneOnly.getLanguage()).isEqualTo("ko"); // 기존 값 유지

            assertThat(languageOnly.getTimezone()).isEqualTo("Asia/Seoul"); // 기존 값 유지
            assertThat(languageOnly.getLanguage()).isEqualTo("en");
        }

        @Test
        @DisplayName("방해금지 시간 업데이트")
        void updateQuietHours_Success() {
            // given
            Map<String, Object> newQuietHours = Map.of(
                "enabled", true,
                "startTime", "23:00",
                "endTime", "07:00",
                "weekendsOnly", true
            );

            // when
            NotificationSettings updated = settings.updateQuietHours(newQuietHours);

            // then
            assertThat(updated.getQuietHours()).containsEntry("enabled", true);
            assertThat(updated.getQuietHours()).containsEntry("startTime", "23:00");
            assertThat(updated.getQuietHours()).containsEntry("endTime", "07:00");
            assertThat(updated.getQuietHours()).containsEntry("weekendsOnly", true);
            assertThat(updated.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("방해금지 시간 비활성화")
        void disableQuietHours_Success() {
            // given
            Map<String, Object> enabledQuietHours = Map.of(
                "enabled", true,
                "startTime", "22:00",
                "endTime", "08:00",
                "weekendsOnly", false
            );
            NotificationSettings settingsWithQuietHours = settings.updateQuietHours(enabledQuietHours);

            // when
            NotificationSettings disabled = settingsWithQuietHours.disableQuietHours();

            // then
            assertThat(disabled.getQuietHours().get("enabled")).isEqualTo(false);
            assertThat(disabled.getQuietHours().get("startTime")).isEqualTo("22:00"); // 기존 값 유지
            assertThat(disabled.getQuietHours().get("endTime")).isEqualTo("08:00");   // 기존 값 유지
            assertThat(disabled.getQuietHours().get("weekendsOnly")).isEqualTo(false); // 기존 값 유지
        }

        @Test
        @DisplayName("모든 알림 비활성화")
        void disableAllNotifications_Success() {
            // when
            NotificationSettings disabled = settings.disableAllNotifications();

            // then
            assertThat(disabled.isStudyUpdates()).isFalse();
            assertThat(disabled.isMarketing()).isFalse();
            assertThat(disabled.isEmailEnabled()).isFalse();
            assertThat(disabled.isDiscordEnabled()).isFalse();
            assertThat(disabled.isPushEnabled()).isFalse();
            assertThat(disabled.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("설정 초기화")
        void reset_Success() {
            // given
            NotificationSettings modified = settings
                .updateEventSettings(false, true)
                .updateChannelSettings(false, true, true)
                .updateLocalizationSettings("Europe/London", "en");

            // when
            NotificationSettings reset = modified.reset();

            // then
            assertThat(reset.isStudyUpdates()).isTrue();
            assertThat(reset.isMarketing()).isFalse();
            assertThat(reset.isEmailEnabled()).isTrue();
            assertThat(reset.isDiscordEnabled()).isFalse();
            assertThat(reset.isPushEnabled()).isFalse();
            assertThat(reset.getTimezone()).isEqualTo("Asia/Seoul");
            assertThat(reset.getLanguage()).isEqualTo("ko");
            assertThat(reset.getCreatedAt()).isEqualTo(settings.getCreatedAt()); // 생성일은 유지
            assertThat(reset.getVersion()).isEqualTo(modified.getVersion() + 1); // 버전은 증가
        }
    }

    @Nested
    @DisplayName("비즈니스 로직 테스트")
    class BusinessLogicTests {

        private NotificationSettings settings;

        @BeforeEach
        void setUp() {
            settings = NotificationSettings.createDefault("user-1");
        }

        @Test
        @DisplayName("채널 활성화 상태 확인")
        void isChannelEnabled_Success() {
            // then
            assertThat(settings.isChannelEnabled(NotificationChannel.ChannelType.EMAIL)).isTrue();
            assertThat(settings.isChannelEnabled(NotificationChannel.ChannelType.DISCORD)).isFalse();
            assertThat(settings.isChannelEnabled(NotificationChannel.ChannelType.PUSH)).isFalse();
        }

        @Test
        @DisplayName("이벤트 타입 활성화 상태 확인")
        void isEventTypeEnabled_Success() {
            // then
            assertThat(settings.isEventTypeEnabled("STUDY_APPROVAL")).isTrue();
            assertThat(settings.isEventTypeEnabled("STUDY_COMMENT")).isTrue();
            assertThat(settings.isEventTypeEnabled("MARKETING_EMAIL")).isFalse();
            assertThat(settings.isEventTypeEnabled("MARKETING_PROMOTION")).isFalse();
            assertThat(settings.isEventTypeEnabled("SYSTEM_NOTICE")).isTrue(); // 시스템 알림은 기본 활성화
            assertThat(settings.isEventTypeEnabled("UNKNOWN_EVENT")).isTrue(); // 알 수 없는 이벤트는 기본 활성화
            assertThat(settings.isEventTypeEnabled(null)).isFalse();
        }

        @Test
        @DisplayName("방해금지 시간 확인 - 비활성화된 경우")
        void isInQuietHours_Disabled() {
            // then
            assertThat(settings.isInQuietHours()).isFalse();
        }

        @Test
        @DisplayName("방해금지 시간 확인 - null 또는 빈 설정")
        void isInQuietHours_NullOrEmpty() {
            // given
            NotificationSettings nullQuietHours = settings.updateQuietHours(null);
            NotificationSettings emptyQuietHours = settings.updateQuietHours(Map.of());

            // then
            assertThat(nullQuietHours.isInQuietHours()).isFalse();
            assertThat(emptyQuietHours.isInQuietHours()).isFalse();
        }

        @Test
        @DisplayName("방해금지 시간 확인 - 잘못된 시간 형식")
        void isInQuietHours_InvalidTimeFormat() {
            // given
            Map<String, Object> invalidQuietHours = Map.of(
                "enabled", true,
                "startTime", "invalid-time",
                "endTime", "invalid-time"
            );
            NotificationSettings settingsWithInvalidTime = settings.updateQuietHours(invalidQuietHours);

            // then
            assertThat(settingsWithInvalidTime.isInQuietHours()).isFalse(); // 파싱 오류 시 false 반환
        }

        @Test
        @DisplayName("알림 허용 여부 확인")
        void isNotificationAllowed_Success() {
            // given
            NotificationSettings allEnabled = settings.updateChannelSettings(true, true, true);

            // then
            assertThat(allEnabled.isNotificationAllowed(
                NotificationChannel.ChannelType.EMAIL, "STUDY_APPROVAL"
            )).isTrue();
            
            assertThat(allEnabled.isNotificationAllowed(
                NotificationChannel.ChannelType.DISCORD, "MARKETING_EMAIL"
            )).isFalse(); // 마케팅 비활성화

            assertThat(settings.isNotificationAllowed(
                NotificationChannel.ChannelType.DISCORD, "STUDY_APPROVAL"
            )).isFalse(); // Discord 비활성화
        }

        @Test
        @DisplayName("활성화된 채널 목록 조회")
        void getEnabledChannels_Success() {
            // given
            NotificationSettings multiChannelEnabled = settings.updateChannelSettings(true, true, false);

            // when
            Set<NotificationChannel.ChannelType> enabledChannels = multiChannelEnabled.getEnabledChannels();

            // then
            assertThat(enabledChannels).hasSize(2);
            assertThat(enabledChannels).contains(
                NotificationChannel.ChannelType.EMAIL,
                NotificationChannel.ChannelType.DISCORD
            );
            assertThat(enabledChannels).doesNotContain(NotificationChannel.ChannelType.PUSH);
        }

        @Test
        @DisplayName("이벤트 타입 활성화 여부 확인")
        void hasAnyEventTypeEnabled_Success() {
            // given
            NotificationSettings noEvents = settings.updateEventSettings(false, false);
            NotificationSettings studyOnly = settings.updateEventSettings(true, false);
            NotificationSettings marketingOnly = settings.updateEventSettings(false, true);

            // then
            assertThat(settings.hasAnyEventTypeEnabled()).isTrue(); // 기본값: study=true, marketing=false
            assertThat(noEvents.hasAnyEventTypeEnabled()).isFalse();
            assertThat(studyOnly.hasAnyEventTypeEnabled()).isTrue();
            assertThat(marketingOnly.hasAnyEventTypeEnabled()).isTrue();
        }

        @Test
        @DisplayName("채널 활성화 여부 확인")
        void hasAnyChannelEnabled_Success() {
            // given
            NotificationSettings noChannels = settings.updateChannelSettings(false, false, false);
            NotificationSettings emailOnly = settings.updateChannelSettings(true, false, false);

            // then
            assertThat(settings.hasAnyChannelEnabled()).isTrue(); // 기본값: email=true
            assertThat(noChannels.hasAnyChannelEnabled()).isFalse();
            assertThat(emailOnly.hasAnyChannelEnabled()).isTrue();
        }

        @Test
        @DisplayName("완전 비활성화 여부 확인")
        void isCompletelyDisabled_Success() {
            // given
            NotificationSettings noEvents = settings.updateEventSettings(false, false);
            NotificationSettings noChannels = settings.updateChannelSettings(false, false, false);
            NotificationSettings completelyDisabled = settings.disableAllNotifications();

            // then
            assertThat(settings.isCompletelyDisabled()).isFalse(); // 기본값은 활성화
            assertThat(noEvents.isCompletelyDisabled()).isTrue();   // 이벤트 없음
            assertThat(noChannels.isCompletelyDisabled()).isTrue();  // 채널 없음
            assertThat(completelyDisabled.isCompletelyDisabled()).isTrue(); // 모두 비활성화
        }

        @Test
        @DisplayName("설정 유효성 검증")
        void isValidSettings_Success() {
            // given
            NotificationSettings invalidUser = NotificationSettings.createDefault("");
            NotificationSettings invalidTimezone = settings.updateLocalizationSettings("", "ko");
            NotificationSettings invalidLanguage = settings.updateLocalizationSettings("Asia/Seoul", "");

            // then
            assertThat(settings.isValidSettings()).isTrue();
            assertThat(invalidUser.isValidSettings()).isFalse();
            assertThat(invalidTimezone.isValidSettings()).isFalse();
            assertThat(invalidLanguage.isValidSettings()).isFalse();
        }

        @Test
        @DisplayName("방해금지 시간 유효성 검증")
        void isValidQuietHours_Success() {
            // given
            Map<String, Object> validQuietHours = Map.of(
                "enabled", true,
                "startTime", "22:00",
                "endTime", "08:00"
            );
            Map<String, Object> invalidQuietHours = Map.of(
                "enabled", true,
                "startTime", "invalid",
                "endTime", "invalid"
            );

            NotificationSettings validSettings = settings.updateQuietHours(validQuietHours);
            NotificationSettings invalidSettings = settings.updateQuietHours(invalidQuietHours);

            // then
            assertThat(validSettings.isValidSettings()).isTrue();
            assertThat(invalidSettings.isValidSettings()).isFalse();
        }
    }
} 