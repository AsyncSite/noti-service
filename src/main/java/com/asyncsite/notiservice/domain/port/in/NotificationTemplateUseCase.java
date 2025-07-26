package com.asyncsite.notiservice.domain.port.in;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface NotificationTemplateUseCase {

    /**
     * 템플릿 목록을 조회합니다.
     */
    List<NotificationTemplate> getTemplates(ChannelType channelType, boolean active);

    /**
     * 템플릿 ID로 템플릿을 조회합니다.
     */
    Optional<NotificationTemplate> getTemplateById(String templateId);

    /**
     * 템플릿을 생성합니다.
     */
    NotificationTemplate createTemplate(
            ChannelType channelType,
            EventType eventType,
            String titleTemplate,
            String contentTemplate,
            Map<String, String> variables
    );

    /**
     * 템플릿을 수정합니다.
     */
    NotificationTemplate updateTemplate(String templateId,
                                        String titleTemplate,
                                        String contentTemplate,
                                        Map<String, String> variables);

    /**
     * 템플릿을 비활성화합니다.
     */
    void deactivateTemplate(String templateId);

    /**
     * 템플릿 미리보기를 생성합니다.
     */
    Map<String, String> previewTemplate(String templateId, Map<String, Object> variables);
}
