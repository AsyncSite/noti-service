package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationTemplateService 테스트")
class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateRepositoryPort templateRepository;

    @InjectMocks
    private NotificationTemplateService templateService;

    @Test
    @DisplayName("필터 조건으로 템플릿 목록을 조회할 수 있다")
    void getTemplates() {
        // given
        ChannelType channelType = ChannelType.EMAIL;
        boolean active = true;
        
        List<NotificationTemplate> expectedTemplates = List.of(
                createTestTemplate("template1"),
                createTestTemplate("template2")
        );

        when(templateRepository.findTemplatesByFilters(channelType, active))
                .thenReturn(expectedTemplates);

        // when
        List<NotificationTemplate> result = templateService.getTemplates(channelType, active);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedTemplates);
        verify(templateRepository).findTemplatesByFilters(channelType, active);
    }

    @Test
    @DisplayName("템플릿 ID로 템플릿을 조회할 수 있다")
    void getTemplateById() {
        // given
        String templateId = "template123";
        NotificationTemplate expectedTemplate = createTestTemplate(templateId);
        
        when(templateRepository.findTemplateById(templateId))
                .thenReturn(Optional.of(expectedTemplate));

        // when
        Optional<NotificationTemplate> result = templateService.getTemplateById(templateId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedTemplate);
        verify(templateRepository).findTemplateById(templateId);
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 ID로 조회시 empty를 반환한다")
    void getTemplateById_NotFound() {
        // given
        String templateId = "nonexistent";
        
        when(templateRepository.findTemplateById(templateId))
                .thenReturn(Optional.empty());

        // when
        Optional<NotificationTemplate> result = templateService.getTemplateById(templateId);

        // then
        assertThat(result).isEmpty();
        verify(templateRepository).findTemplateById(templateId);
    }

    @Test
    @DisplayName("새로운 템플릿을 생성할 수 있다")
    void createTemplate() {
        // given
        ChannelType channelType = ChannelType.EMAIL;
        EventType eventType = EventType.STUDY;
        String titleTemplate = "Hello {userName}!";
        String contentTemplate = "Your progress: {progress}%";
        Map<String, String> variables = Map.of("userName", "DefaultUser");
        
        NotificationTemplate expectedTemplate = NotificationTemplate.create(
                channelType, eventType, titleTemplate, contentTemplate, variables
        );

        when(templateRepository.saveTemplate(any(NotificationTemplate.class)))
                .thenReturn(expectedTemplate);

        // when
        NotificationTemplate result = templateService.createTemplate(
                channelType, eventType, titleTemplate, contentTemplate, variables
        );

        // then
        assertThat(result.getChannelType()).isEqualTo(channelType);
        assertThat(result.getEventType()).isEqualTo(eventType);
        assertThat(result.getTitleTemplate()).isEqualTo(titleTemplate);
        assertThat(result.getContentTemplate()).isEqualTo(contentTemplate);
        assertThat(result.getVariables()).isEqualTo(variables);
        
        verify(templateRepository).saveTemplate(any(NotificationTemplate.class));
    }

    @Test
    @DisplayName("기존 템플릿을 수정할 수 있다")
    void updateTemplate() {
        // given
        String templateId = "template123";
        String newTitleTemplate = "Hi {userName}!";
        String newContentTemplate = "Updated progress: {progress}%";
        Map<String, String> newVariables = Map.of("userName", "NewUser");
        
        NotificationTemplate existingTemplate = createTestTemplate(templateId);
        NotificationTemplate updatedTemplate = existingTemplate.updateTemplate(
                newTitleTemplate, newContentTemplate, newVariables
        );

        when(templateRepository.findTemplateById(templateId))
                .thenReturn(Optional.of(existingTemplate));
        when(templateRepository.saveTemplate(any(NotificationTemplate.class)))
                .thenReturn(updatedTemplate);

        // when
        NotificationTemplate result = templateService.updateTemplate(
                templateId, newTitleTemplate, newContentTemplate, newVariables
        );

        // then
        assertThat(result.getTitleTemplate()).isEqualTo(newTitleTemplate);
        assertThat(result.getContentTemplate()).isEqualTo(newContentTemplate);
        assertThat(result.getVariables()).isEqualTo(newVariables);
        
        verify(templateRepository).findTemplateById(templateId);
        verify(templateRepository).saveTemplate(any(NotificationTemplate.class));
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 수정시 예외가 발생한다")
    void updateTemplate_NotFound() {
        // given
        String templateId = "nonexistent";
        
        when(templateRepository.findTemplateById(templateId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {
            templateService.updateTemplate(templateId, "title", "content", Map.of());
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("템플릿을 찾을 수 없습니다");
        
        verify(templateRepository).findTemplateById(templateId);
        verify(templateRepository, never()).saveTemplate(any());
    }

    @Test
    @DisplayName("템플릿을 비활성화할 수 있다")
    void deactivateTemplate() {
        // given
        String templateId = "template123";
        NotificationTemplate existingTemplate = createTestTemplate(templateId);
        NotificationTemplate deactivatedTemplate = existingTemplate.deactivate();

        when(templateRepository.findTemplateById(templateId))
                .thenReturn(Optional.of(existingTemplate));
        when(templateRepository.saveTemplate(any(NotificationTemplate.class)))
                .thenReturn(deactivatedTemplate);

        // when
        templateService.deactivateTemplate(templateId);

        // then
        verify(templateRepository).findTemplateById(templateId);
        verify(templateRepository).saveTemplate(any(NotificationTemplate.class));
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 비활성화시 예외가 발생한다")
    void deactivateTemplate_NotFound() {
        // given
        String templateId = "nonexistent";
        
        when(templateRepository.findTemplateById(templateId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {
            templateService.deactivateTemplate(templateId);
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("템플릿을 찾을 수 없습니다");
        
        verify(templateRepository).findTemplateById(templateId);
        verify(templateRepository, never()).saveTemplate(any());
    }

    @Test
    @DisplayName("템플릿 미리보기를 생성할 수 있다")
    void previewTemplate() {
        // given
        String templateId = "template123";
        NotificationTemplate template = createTestTemplate(templateId);
        Map<String, Object> variables = Map.of(
                "userName", "John",
                "progress", 85
        );

        when(templateRepository.findTemplateById(templateId))
                .thenReturn(Optional.of(template));

        // when
        Map<String, String> result = templateService.previewTemplate(templateId, variables);

        // then
        assertThat(result).containsKey("title");
        assertThat(result).containsKey("content");
        assertThat(result.get("title")).isEqualTo("Hello John!");
        assertThat(result.get("content")).isEqualTo("Your progress: 85%");
        
        verify(templateRepository).findTemplateById(templateId);
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 미리보기시 예외가 발생한다")
    void previewTemplate_NotFound() {
        // given
        String templateId = "nonexistent";
        
        when(templateRepository.findTemplateById(templateId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {
            templateService.previewTemplate(templateId, Map.of());
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("템플릿을 찾을 수 없습니다");
        
        verify(templateRepository).findTemplateById(templateId);
    }

    private NotificationTemplate createTestTemplate(String templateId) {
        return NotificationTemplate.builder()
                .templateId(templateId)
                .channelType(ChannelType.EMAIL)
                .eventType(EventType.STUDY)
                .titleTemplate("Hello {userName}!")
                .contentTemplate("Your progress: {progress}%")
                .variables(Map.of("userName", "DefaultUser", "progress", "0"))
                .active(true)
                .version(0)
                .build();
    }
} 