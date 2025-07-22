package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.port.in.NotificationTemplateUseCase;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<NotificationTemplate> getTemplates(String eventType, String channelType, String language, boolean isActive, int page, int size) {
        log.info("템플릿 목록 조회: eventType={}, channelType={}, language={}, isActive={}, page={}, size={}",
                eventType, channelType, language, isActive, page, size);

        NotificationChannel.ChannelType channelTypeEnum = null;
        if (channelType != null) {
            try {
                channelTypeEnum = NotificationChannel.ChannelType.valueOf(channelType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 채널 타입: {}", channelType);
                return List.of();
            }
        }

        return templateRepository.findTemplatesByFilters(eventType, channelTypeEnum, language, isActive, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationTemplate> getTemplateById(String templateId) {
        log.info("템플릿 조회: templateId={}", templateId);
        return templateRepository.findTemplateById(templateId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationTemplate> getTemplateByEventAndChannel(String eventType, String channelType, String language) {
        log.info("템플릿 조회: eventType={}, channelType={}, language={}", eventType, channelType, language);

        try {
            NotificationChannel.ChannelType channelTypeEnum = NotificationChannel.ChannelType.valueOf(channelType.toUpperCase());
            return templateRepository.findTemplateByEventAndChannel(eventType, channelTypeEnum, language);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 채널 타입: {}", channelType);
            return Optional.empty();
        }
    }

    @Override
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        log.info("템플릿 생성: eventType={}, channelType={}, language={}",
                template.getEventType(), template.getChannelType(), template.getLanguage());

        // 도메인 팩토리 메서드 사용이 이미 완료된 template을 그대로 저장
        return templateRepository.saveTemplate(template);
    }

    @Override
    public NotificationTemplate updateTemplate(String templateId, NotificationTemplate updateData) {
        log.info("템플릿 수정: templateId={}", templateId);

        Optional<NotificationTemplate> existingTemplate = templateRepository.findTemplateById(templateId);
        if (existingTemplate.isEmpty()) {
            throw new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + templateId);
        }

        NotificationTemplate existing = existingTemplate.get();
        
        // 도메인 행위 메서드 사용
        NotificationTemplate updatedTemplate = existing.updateTemplate(
                updateData.getTitleTemplate(),
                updateData.getContentTemplate(),
                updateData.getVariables()
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
    public NotificationTemplate cloneTemplate(String templateId, String language, String titleTemplate, String contentTemplate) {
        log.info("템플릿 복제: templateId={}, language={}", templateId, language);

        Optional<NotificationTemplate> originalTemplate = templateRepository.findTemplateById(templateId);
        if (originalTemplate.isEmpty()) {
            throw new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + templateId);
        }

        NotificationTemplate original = originalTemplate.get();
        
        // 도메인 행위 메서드 사용
        NotificationTemplate clonedTemplate = original.cloneForLanguage(language, titleTemplate, contentTemplate);

        return templateRepository.saveTemplate(clonedTemplate);
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
