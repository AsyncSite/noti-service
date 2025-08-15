package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepositoryPort {

    /**
     * 템플릿을 저장합니다.
     */
    NotificationTemplate saveTemplate(NotificationTemplate template);

    /**
     * 템플릿 ID로 템플릿을 조회합니다.
     */
    Optional<NotificationTemplate> findTemplateById(String templateId);

    /**
     * 채널 타입으로 템플릿을 조회합니다.
     */
    List<NotificationTemplate> findTemplateByChannel(ChannelType channelType);
    List<NotificationTemplate> findTemplates();

    // 템플릿 선택은 templateId로만 수행. (channel,event) 조회는 제거

    /**
     * 필터 조건으로 템플릿 목록을 조회합니다.
     */
    List<NotificationTemplate> findTemplatesByFilters(
            ChannelType channelType,
            Boolean isActive);
}
