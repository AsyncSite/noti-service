package com.asyncsite.notiservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationTemplate 도메인 모델 테스트")
class NotificationTemplateTest {

    @Nested
    @DisplayName("템플릿 생성 테스트")
    class CreateTemplateTests {

        @Test
        @DisplayName("일반 템플릿 생성 - 성공")
        void create_Success() {
            // given
            Map<String, String> variables = Map.of(
                "userName", "기본 사용자",
                "studyName", "기본 스터디"
            );

            // when
            NotificationTemplate template = NotificationTemplate.create(
                "STUDY_APPROVAL",
                NotificationChannel.ChannelType.EMAIL,
                "ko",
                "{{userName}}님의 {{studyName}} 스터디가 승인되었습니다",
                "안녕하세요 {{userName}}님,\n\n{{studyName}} 스터디가 승인되었습니다.",
                variables
            );

            // then
            assertThat(template).isNotNull();
            assertThat(template.getTemplateId()).isNotNull();
            assertThat(template.getEventType()).isEqualTo("STUDY_APPROVAL");
            assertThat(template.getChannelType()).isEqualTo(NotificationChannel.ChannelType.EMAIL);
            assertThat(template.getLanguage()).isEqualTo("ko");
            assertThat(template.getTitleTemplate()).isEqualTo("{{userName}}님의 {{studyName}} 스터디가 승인되었습니다");
            assertThat(template.getContentTemplate()).isEqualTo("안녕하세요 {{userName}}님,\n\n{{studyName}} 스터디가 승인되었습니다.");
            assertThat(template.getVariables()).containsEntry("userName", "기본 사용자");
            assertThat(template.getVariables()).containsEntry("studyName", "기본 스터디");
            assertThat(template.isActive()).isTrue();
            assertThat(template.getVersion()).isEqualTo(1);
            assertThat(template.getVersion()).isEqualTo(0);
            assertThat(template.getCreatedAt()).isNotNull();
            assertThat(template.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("이메일 템플릿 생성")
        void createEmailTemplate_Success() {
            // when
            NotificationTemplate template = NotificationTemplate.createEmailTemplate(
                "STUDY_COMMENT",
                "ko",
                "새로운 댓글이 등록되었습니다",
                "{{authorName}}님이 댓글을 작성했습니다: {{content}}",
                Map.of("authorName", "익명", "content", "내용 없음")
            );

            // then
            assertThat(template.getChannelType()).isEqualTo(NotificationChannel.ChannelType.EMAIL);
            assertThat(template.getEventType()).isEqualTo("STUDY_COMMENT");
        }

        @Test
        @DisplayName("Discord 템플릿 생성")
        void createDiscordTemplate_Success() {
            // when
            NotificationTemplate template = NotificationTemplate.createDiscordTemplate(
                "STUDY_JOIN",
                "ko",
                "스터디 참가 알림",
                "**{{userName}}**님이 {{studyName}} 스터디에 참가했습니다!",
                Map.of("userName", "사용자", "studyName", "스터디")
            );

            // then
            assertThat(template.getChannelType()).isEqualTo(NotificationChannel.ChannelType.DISCORD);
            assertThat(template.getEventType()).isEqualTo("STUDY_JOIN");
        }

        @Test
        @DisplayName("푸시 알림 템플릿 생성")
        void createPushTemplate_Success() {
            // when
            NotificationTemplate template = NotificationTemplate.createPushTemplate(
                "MARKETING_PROMOTION",
                "ko",
                "특별 혜택!",
                "{{discount}}% 할인 이벤트가 진행 중입니다!",
                Map.of("discount", "20")
            );

            // then
            assertThat(template.getChannelType()).isEqualTo(NotificationChannel.ChannelType.PUSH);
            assertThat(template.getEventType()).isEqualTo("MARKETING_PROMOTION");
        }
    }

    @Nested
    @DisplayName("템플릿 업데이트 테스트")
    class UpdateTemplateTests {

        private NotificationTemplate template;

        @BeforeEach
        void setUp() {
            template = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "원본 제목",
                "원본 내용",
                Map.of("variable1", "value1")
            );
        }

        @Test
        @DisplayName("템플릿 업데이트")
        void updateTemplate_Success() {
            // given
            Map<String, String> newVariables = Map.of(
                "variable1", "updated_value1",
                "variable2", "value2"
            );

            // when
            NotificationTemplate updated = template.updateTemplate(
                "업데이트된 제목",
                "업데이트된 내용",
                newVariables
            );

            // then
            assertThat(updated.getTitleTemplate()).isEqualTo("업데이트된 제목");
            assertThat(updated.getContentTemplate()).isEqualTo("업데이트된 내용");
            assertThat(updated.getVariables()).containsEntry("variable1", "updated_value1");
            assertThat(updated.getVariables()).containsEntry("variable2", "value2");
            assertThat(updated.getVersion()).isEqualTo(2);
            assertThat(updated.getVersion()).isEqualTo(1);
            assertThat(updated.getUpdatedAt()).isAfter(template.getUpdatedAt());
        }

        @Test
        @DisplayName("템플릿 활성화")
        void activate_Success() {
            // given
            NotificationTemplate deactivated = template.deactivate();

            // when
            NotificationTemplate activated = deactivated.activate();

            // then
            assertThat(activated.isActive()).isTrue();
            assertThat(activated.getVersion()).isEqualTo(2); // deactivate -> activate
            assertThat(activated.getUpdatedAt()).isAfter(deactivated.getUpdatedAt());
        }

        @Test
        @DisplayName("템플릿 비활성화")
        void deactivate_Success() {
            // when
            NotificationTemplate deactivated = template.deactivate();

            // then
            assertThat(deactivated.isActive()).isFalse();
            assertThat(deactivated.getVersion()).isEqualTo(1);
            assertThat(deactivated.getUpdatedAt()).isAfter(template.getUpdatedAt());
        }

        @Test
        @DisplayName("새 버전 템플릿 생성")
        void createNewVersion_Success() {
            // given
            Map<String, String> newVariables = Map.of("newVar", "newValue");

            // when
            NotificationTemplate newVersion = template.createNewVersion(
                "새 버전 제목",
                "새 버전 내용",
                newVariables
            );

            // then
            assertThat(newVersion.getTitleTemplate()).isEqualTo("새 버전 제목");
            assertThat(newVersion.getContentTemplate()).isEqualTo("새 버전 내용");
            assertThat(newVersion.getVariables()).containsEntry("newVar", "newValue");
            assertThat(newVersion.getVersion()).isEqualTo(2);
            assertThat(newVersion.getVersion()).isEqualTo(1);
            assertThat(newVersion.getTemplateId()).isEqualTo(template.getTemplateId()); // 같은 템플릿 ID 유지
        }

        @Test
        @DisplayName("다른 언어로 템플릿 복제")
        void cloneForLanguage_Success() {
            // when
            NotificationTemplate cloned = template.cloneForLanguage(
                "en",
                "English Title",
                "English Content"
            );

            // then
            assertThat(cloned.getTemplateId()).isNotEqualTo(template.getTemplateId()); // 새로운 ID 생성
            assertThat(cloned.getEventType()).isEqualTo(template.getEventType());
            assertThat(cloned.getChannelType()).isEqualTo(template.getChannelType());
            assertThat(cloned.getLanguage()).isEqualTo("en");
            assertThat(cloned.getTitleTemplate()).isEqualTo("English Title");
            assertThat(cloned.getContentTemplate()).isEqualTo("English Content");
            assertThat(cloned.getVariables()).isEqualTo(template.getVariables()); // 변수는 동일
            assertThat(cloned.getVersion()).isEqualTo(1); // 새로운 템플릿이므로 버전 1
            assertThat(cloned.getVersion()).isEqualTo(0);
            assertThat(cloned.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("템플릿 렌더링 테스트")
    class RenderingTests {

        private NotificationTemplate template;

        @BeforeEach
        void setUp() {
            template = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "{{userName}}님께 {{itemName}} 알림",
                "안녕하세요 {{userName}}님,\n\n{{itemName}}에 대한 알림입니다.\n감사합니다.",
                Map.of("userName", "기본사용자", "itemName", "기본아이템")
            );
        }

        @Test
        @DisplayName("제목 렌더링 - 성공")
        void renderTitle_Success() {
            // given
            Map<String, Object> data = Map.of(
                "userName", "홍길동",
                "itemName", "스터디 승인"
            );

            // when
            String renderedTitle = template.renderTitle(data);

            // then
            assertThat(renderedTitle).isEqualTo("홍길동님께 스터디 승인 알림");
        }

        @Test
        @DisplayName("내용 렌더링 - 성공")
        void renderContent_Success() {
            // given
            Map<String, Object> data = Map.of(
                "userName", "김철수",
                "itemName", "댓글 등록"
            );

            // when
            String renderedContent = template.renderContent(data);

            // then
            assertThat(renderedContent).isEqualTo("안녕하세요 김철수님,\n\n댓글 등록에 대한 알림입니다.\n감사합니다.");
        }

        @Test
        @DisplayName("변수 값이 없는 경우 기본값 사용")
        void render_WithMissingVariables_UseDefaults() {
            // given
            Map<String, Object> data = Map.of("userName", "이영희"); // itemName 누락

            // when
            String renderedTitle = template.renderTitle(data);
            String renderedContent = template.renderContent(data);

            // then
            assertThat(renderedTitle).isEqualTo("이영희님께 기본아이템 알림"); // 기본값 사용
            assertThat(renderedContent).isEqualTo("안녕하세요 이영희님,\n\n기본아이템에 대한 알림입니다.\n감사합니다.");
        }

        @Test
        @DisplayName("기본값도 없는 경우 빈 문자열 사용")
        void render_WithoutDefaultValues_UseEmptyString() {
            // given
            NotificationTemplate templateWithoutDefaults = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "{{userName}}님께 {{unknownVar}} 알림",
                "내용: {{unknownVar}}",
                Map.of("userName", "기본사용자") // unknownVar에 대한 기본값 없음
            );
            Map<String, Object> data = Map.of("userName", "홍길동");

            // when
            String renderedTitle = templateWithoutDefaults.renderTitle(data);
            String renderedContent = templateWithoutDefaults.renderContent(data);

            // then
            assertThat(renderedTitle).isEqualTo("홍길동님께  알림"); // 빈 문자열로 대체
            assertThat(renderedContent).isEqualTo("내용: ");
        }

        @Test
        @DisplayName("null 또는 빈 템플릿 렌더링")
        void render_NullOrEmptyTemplate() {
            // given
            NotificationTemplate emptyTemplate = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "",
                null,
                Map.of()
            );

            // when
            String renderedTitle = emptyTemplate.renderTitle(Map.of());
            String renderedContent = emptyTemplate.renderContent(Map.of());

            // then
            assertThat(renderedTitle).isEqualTo("");
            assertThat(renderedContent).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("비즈니스 로직 테스트")
    class BusinessLogicTests {

        private NotificationTemplate template;

        @BeforeEach
        void setUp() {
            template = NotificationTemplate.createEmailTemplate(
                "STUDY_APPROVAL",
                "ko",
                "스터디 승인 알림",
                "스터디가 승인되었습니다.",
                Map.of()
            );
        }

        @Test
        @DisplayName("이벤트 타입 확인")
        void isForEventType_Success() {
            // then
            assertThat(template.isForEventType("STUDY_APPROVAL")).isTrue();
            assertThat(template.isForEventType("STUDY_COMMENT")).isFalse();
            assertThat(template.isForEventType(null)).isFalse();
        }

        @Test
        @DisplayName("채널 타입 확인")
        void isForChannelType_Success() {
            // then
            assertThat(template.isForChannelType(NotificationChannel.ChannelType.EMAIL)).isTrue();
            assertThat(template.isForChannelType(NotificationChannel.ChannelType.DISCORD)).isFalse();
            assertThat(template.isForChannelType(NotificationChannel.ChannelType.PUSH)).isFalse();
        }

        @Test
        @DisplayName("언어 확인")
        void isForLanguage_Success() {
            // then
            assertThat(template.isForLanguage("ko")).isTrue();
            assertThat(template.isForLanguage("en")).isFalse();
            assertThat(template.isForLanguage(null)).isFalse();
        }

        @Test
        @DisplayName("활성화 상태 확인")
        void isActive_Success() {
            // given
            NotificationTemplate activeTemplate = template;
            NotificationTemplate inactiveTemplate = template.deactivate();

            // then
            assertThat(activeTemplate.isActive()).isTrue();
            assertThat(inactiveTemplate.isActive()).isFalse();
        }

        @Test
        @DisplayName("사용 가능 여부 확인")
        void isUsable_Success() {
            // given
            NotificationTemplate activeValidTemplate = template;
            NotificationTemplate inactiveTemplate = template.deactivate();
            NotificationTemplate invalidTemplate = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "{{unclosed", // 잘못된 템플릿
                "{{also_unclosed",
                Map.of()
            );

            // then
            assertThat(activeValidTemplate.isUsable()).isTrue();
            assertThat(inactiveTemplate.isUsable()).isFalse(); // 비활성화
            assertThat(invalidTemplate.isUsable()).isFalse();   // 잘못된 템플릿
        }

        @Test
        @DisplayName("템플릿 유효성 검증")
        void isValidTemplate_Success() {
            // given
            NotificationTemplate validTemplate = template;
            NotificationTemplate invalidTitleTemplate = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "{{unclosed",
                "valid content",
                Map.of()
            );
            NotificationTemplate invalidContentTemplate = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "valid title",
                "{{unclosed",
                Map.of()
            );
            NotificationTemplate emptyTemplate = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "",
                "",
                Map.of()
            );

            // then
            assertThat(validTemplate.isValidTemplate()).isTrue();
            assertThat(invalidTitleTemplate.isValidTemplate()).isFalse();
            assertThat(invalidContentTemplate.isValidTemplate()).isFalse();
            assertThat(emptyTemplate.isValidTemplate()).isFalse();
        }

        @Test
        @DisplayName("변수 추출")
        void extractVariables_Success() {
            // given
            NotificationTemplate complexTemplate = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "{{userName}}님과 {{studyName}} 관련",
                "안녕하세요 {{userName}}님,\n{{studyName}}에서 {{eventType}} 이벤트가 발생했습니다.\n{{additionalInfo}}",
                Map.of()
            );

            // when
            Set<String> variables = complexTemplate.extractVariables();

            // then
            assertThat(variables).hasSize(4);
            assertThat(variables).contains("userName", "studyName", "eventType", "additionalInfo");
        }

        @Test
        @DisplayName("조건 일치 확인")
        void matches_Success() {
            // then
            assertThat(template.matches(
                "STUDY_APPROVAL",
                NotificationChannel.ChannelType.EMAIL,
                "ko"
            )).isTrue();

            assertThat(template.matches(
                "STUDY_COMMENT", // 다른 이벤트
                NotificationChannel.ChannelType.EMAIL,
                "ko"
            )).isFalse();

            assertThat(template.matches(
                "STUDY_APPROVAL",
                NotificationChannel.ChannelType.DISCORD, // 다른 채널
                "ko"
            )).isFalse();

            assertThat(template.matches(
                "STUDY_APPROVAL",
                NotificationChannel.ChannelType.EMAIL,
                "en" // 다른 언어
            )).isFalse();
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("복잡한 중괄호 패턴 처리")
        void complexBracePatterns() {
            // given
            NotificationTemplate template = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "일반 텍스트 {단일중괄호} {{정상변수}} {{{비정상}}} 끝",
                "내용: {{var1}} {비정상} {{var2}} end",
                Map.of("정상변수", "정상값", "var1", "값1", "var2", "값2")
            );
            Map<String, Object> data = Map.of(
                "정상변수", "렌더링값",
                "var1", "데이터1",
                "var2", "데이터2"
            );

            // when
            String renderedTitle = template.renderTitle(data);
            String renderedContent = template.renderContent(data);

            // then
            // {{{비정상}}}에서 {{비정상}}이 빈 문자열로 대체되어 {}가 남고, 다시 처리되어 }만 남음
            assertThat(renderedTitle).isEqualTo("일반 텍스트 {단일중괄호} 렌더링값 } 끝");
            assertThat(renderedContent).isEqualTo("내용: 데이터1 {비정상} 데이터2 end");
        }

        @Test
        @DisplayName("특수 문자가 포함된 변수 처리")
        void specialCharactersInVariables() {
            // given
            NotificationTemplate template = NotificationTemplate.createEmailTemplate(
                "TEST_EVENT",
                "ko",
                "{{user_name}}의 {{study-name}} ({{user.email}})",
                "{{description}}",
                Map.of()
            );
            Map<String, Object> data = Map.of(
                "user_name", "홍길동",
                "study-name", "Java 스터디",
                "user.email", "hong@example.com",
                "description", "특수문자 포함 설명"
            );

            // when
            String renderedTitle = template.renderTitle(data);
            String renderedContent = template.renderContent(data);

            // then
            assertThat(renderedTitle).isEqualTo("홍길동의 Java 스터디 (hong@example.com)");
            assertThat(renderedContent).isEqualTo("특수문자 포함 설명");
        }
    }
} 