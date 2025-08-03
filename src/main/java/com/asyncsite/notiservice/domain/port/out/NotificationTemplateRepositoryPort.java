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

    /**
     * 이벤트 타입과 채널 타입으로 템플릿을 조회합니다.
     */
    Optional<NotificationTemplate> findTemplateByChannelAndEventType(ChannelType channelType, EventType eventType);

    /**
     * 필터 조건으로 템플릿 목록을 조회합니다.
     */
    List<NotificationTemplate> findTemplatesByFilters(
            ChannelType channelType,
            Boolean isActive);
}
