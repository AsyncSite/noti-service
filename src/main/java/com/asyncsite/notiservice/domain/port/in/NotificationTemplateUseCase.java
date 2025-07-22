package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface NotificationTemplateUseCase {

    /**
     * 템플릿 목록을 조회합니다.
     */
    List<NotificationTemplate> getTemplates(String eventType, String channelType, String language, boolean isActive, int page, int size);

    /**
     * 템플릿 ID로 템플릿을 조회합니다.
     */
    Optional<NotificationTemplate> getTemplateById(String templateId);

    /**
     * 채널 타입으로 템플릿을 조회합니다.
     */
    Optional<NotificationTemplate> getTemplateByEventAndChannel(String channelType);

    /**
     * 템플릿을 생성합니다.
     */
    NotificationTemplate createTemplate(NotificationTemplate template);

    /**
     * 템플릿을 수정합니다.
     */
    NotificationTemplate updateTemplate(String templateId, NotificationTemplate template);

    /**
     * 템플릿을 비활성화합니다.
     */
    void deactivateTemplate(String templateId);

    /**
     * 템플릿 미리보기를 생성합니다.
     */
    Map<String, String> previewTemplate(String templateId, Map<String, Object> variables);
}
