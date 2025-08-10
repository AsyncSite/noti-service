package com.asyncsite.notiservice.domain.model;

import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationTemplate 도메인 모델 테스트")
class NotificationTemplateTest {

    @Test
    @DisplayName("새로운 알림 템플릿을 생성할 수 있다")
    void createTemplate() {
        // given
        ChannelType channelType = ChannelType.EMAIL;
        EventType eventType = EventType.STUDY;
        String titleTemplate = "Hello {userName}!";
        String contentTemplate = "Your study progress: {progress}%";
        Map<String, String> variables = Map.of(
                "userName", "DefaultUser",
                "progress", "0"
        );

        // when
        NotificationTemplate template = NotificationTemplate.create(
                channelType, eventType, titleTemplate, contentTemplate, variables
        );

        // then
        assertThat(template.getChannelType()).isEqualTo(channelType);
        assertThat(template.getEventType()).isEqualTo(eventType);
        assertThat(template.getTitleTemplate()).isEqualTo(titleTemplate);
        assertThat(template.getContentTemplate()).isEqualTo(contentTemplate);
        assertThat(template.getVariables()).isEqualTo(variables);
        assertThat(template.isActive()).isTrue();
        assertThat(template.getVersion()).isEqualTo(0);
        assertThat(template.getCreatedAt()).isNotNull();
        assertThat(template.getUpdatedAt()).isNotNull(); // 생성시에는 null
    }

    @Test
    @DisplayName("템플릿을 업데이트할 수 있다")
    void updateTemplate() {
        // given
        NotificationTemplate template = createTestTemplate();
        String newTitleTemplate = "Hi {userName}!";
        String newContentTemplate = "Your new progress: {progress}%";
        Map<String, String> newVariables = Map.of("userName", "NewUser");

        // when
        NotificationTemplate updatedTemplate = template.updateTemplate(
                newTitleTemplate, newContentTemplate, newVariables
        );

        // then
        assertThat(updatedTemplate.getTitleTemplate()).isEqualTo(newTitleTemplate);
        assertThat(updatedTemplate.getContentTemplate()).isEqualTo(newContentTemplate);
        assertThat(updatedTemplate.getVariables()).isEqualTo(newVariables);
        assertThat(updatedTemplate.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("템플릿을 활성화할 수 있다")
    void activateTemplate() {
        // given
        NotificationTemplate template = createTestTemplate().deactivate();

        // when
        NotificationTemplate activatedTemplate = template.activate();

        // then
        assertThat(activatedTemplate.isActive()).isTrue();
        assertThat(activatedTemplate.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("템플릿을 비활성화할 수 있다")
    void deactivateTemplate() {
        // given
        NotificationTemplate template = createTestTemplate();

        // when
        NotificationTemplate deactivatedTemplate = template.deactivate();

        // then
        assertThat(deactivatedTemplate.isActive()).isFalse();
        assertThat(deactivatedTemplate.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("제목을 렌더링할 수 있다")
    void renderTitle() {
        // given
        NotificationTemplate template = createTestTemplate();
        Map<String, Object> data = Map.of(
                "userName", "John",
                "progress", 75
        );

        // when
        String renderedTitle = template.renderTitle(data);

        // then
        assertThat(renderedTitle).isEqualTo("Hello John!");
    }

    @Test
    @DisplayName("내용을 렌더링할 수 있다")
    void renderContent() {
        // given
        NotificationTemplate template = createTestTemplate();
        Map<String, Object> data = Map.of(
                "userName", "John",
                "progress", 75
        );

        // when
        String renderedContent = template.renderContent(data);

        // then
        assertThat(renderedContent).isEqualTo("Your study progress: 75%");
    }

    @Test
    @DisplayName("변수가 없는 경우 기본값을 사용한다")
    void renderWithDefaultValue() {
        // given
        NotificationTemplate template = createTestTemplate();
        Map<String, Object> data = Map.of("progress", 75); // userName 누락

        // when
        String renderedTitle = template.renderTitle(data);

        // then
        assertThat(renderedTitle).isEqualTo("Hello DefaultUser!"); // 기본값 사용
    }

    @Test
    @DisplayName("데이터와 기본값 모두 없는 경우 빈 문자열을 사용한다")
    void renderWithNoValueAndNoDefault() {
        // given
        NotificationTemplate template = NotificationTemplate.create(
                ChannelType.EMAIL,
                EventType.STUDY,
                "Hello {unknown}!",
                "Content",
                Map.of()
        );
        Map<String, Object> data = Map.of();

        // when
        String renderedTitle = template.renderTitle(data);

        // then
        assertThat(renderedTitle).isEqualTo("Hello !"); // 빈 문자열 사용
    }

    @Test
    @DisplayName("유효한 템플릿인지 확인할 수 있다")
    void isValidTemplate() {
        // given
        NotificationTemplate validTemplate = createTestTemplate();
        NotificationTemplate invalidTemplate = NotificationTemplate.create(
                ChannelType.EMAIL,
                EventType.STUDY,
                "Hello {userName", // 중괄호 미완성
                "Content",
                Map.of()
        );

        // then
        assertThat(validTemplate.isValidTemplate()).isTrue();
        assertThat(invalidTemplate.isValidTemplate()).isFalse();
    }

    @Test
    @DisplayName("템플릿이 사용 가능한지 확인할 수 있다")
    void isUsable() {
        // given
        NotificationTemplate activeValidTemplate = createTestTemplate();
        NotificationTemplate inactiveTemplate = createTestTemplate().deactivate();

        // then
        assertThat(activeValidTemplate.isUsable()).isTrue();
        assertThat(inactiveTemplate.isUsable()).isFalse();
    }

    @Test
    @DisplayName("템플릿에서 변수 목록을 추출할 수 있다")
    void extractVariables() {
        // given
        NotificationTemplate template = NotificationTemplate.create(
                ChannelType.EMAIL,
                EventType.STUDY,
                "Hello {userName} and {adminName}!",
                "Your progress: {progress}% completed by {userName}",
                Map.of()
        );

        // when
        var variables = template.extractVariables();

        // then
        assertThat(variables).containsExactlyInAnyOrder("userName", "adminName", "progress");
    }

    private NotificationTemplate createTestTemplate() {
        return NotificationTemplate.create(
                ChannelType.EMAIL,
                EventType.STUDY,
                "Hello {userName}!",
                "Your study progress: {progress}%",
                Map.of(
                        "userName", "DefaultUser",
                        "progress", "0"
                )
        );
    }
}
