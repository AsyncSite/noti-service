package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.in.NotificationTemplateUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationTemplateService implements NotificationTemplateUseCase {

    private final NotificationTemplateRepositoryPort templateRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getTemplates(String channelType, boolean active) {
        if(Strings.isEmpty(channelType))
            return templateRepository.findTemplates();
        return templateRepository.findTemplatesByFilters(ChannelType.valueOf(channelType), active);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationTemplate> getTemplateById(String templateId) {
        log.info("템플릿 조회: templateId={}", templateId);
        return templateRepository.findTemplateById(templateId);
    }

    @Override
    public NotificationTemplate createTemplate(
            ChannelType channelType,
            EventType eventType,
            String titleTemplate,
            String contentTemplate,
            Map<String, String> variables
    ) {
        // 도메인 팩토리 메서드 사용이 이미 완료된 template을 그대로 저장
        return templateRepository.saveTemplate(NotificationTemplate.create(channelType, eventType, titleTemplate, contentTemplate, variables));
    }

    @Override
    public NotificationTemplate updateTemplate(
            String templateId,
            String titleTemplate,
            String contentTemplate,
            Map<String, String> variables
    ) {
        log.info("템플릿 수정: templateId={}", templateId);

        Optional<NotificationTemplate> existingTemplate = templateRepository.findTemplateById(templateId);
        if (existingTemplate.isEmpty()) {
            throw new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + templateId);
        }

        NotificationTemplate existing = existingTemplate.get();

        // 도메인 행위 메서드 사용
        NotificationTemplate updatedTemplate = existing.updateTemplate(
                titleTemplate,
                contentTemplate,
                variables
        );

        return templateRepository.saveTemplate(updatedTemplate);
    }

    @Override
    public void deactivateTemplate(String templateId) {
        log.info("템플릿 비활성화: templateId={}", templateId);

        Optional<NotificationTemplate> template = templateRepository.findTemplateById(templateId);
        if (template.isEmpty()) {
            throw new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + templateId);
        }

        // 도메인 행위 메서드 사용
        NotificationTemplate deactivatedTemplate = template.get().deactivate();

        templateRepository.saveTemplate(deactivatedTemplate);
    }

    @Override
    public Map<String, String> previewTemplate(String templateId, Map<String, Object> variables) {
        log.info("템플릿 미리보기: templateId={}", templateId);

        Optional<NotificationTemplate> template = templateRepository.findTemplateById(templateId);
        if (template.isEmpty()) {
            throw new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + templateId);
        }

        NotificationTemplate templateData = template.get();
        String renderedTitle = templateData.renderTitle(variables);
        String renderedContent = templateData.renderContent(variables);

        Map<String, String> preview = new HashMap<>();
        preview.put("title", renderedTitle);
        preview.put("content", renderedContent);

        return preview;
    }
}
