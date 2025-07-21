package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;

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
     * 이벤트 타입과 채널 타입으로 템플릿을 조회합니다.
     */
    Optional<NotificationTemplate> findTemplateByEventAndChannel(String eventType,
                                                                com.asyncsite.notiservice.domain.model.NotificationChannel.ChannelType channelType,
                                                                String language);

    /**
     * 필터 조건으로 템플릿 목록을 조회합니다.
     */
    List<NotificationTemplate> findTemplatesByFilters(String eventType,
                                                     com.asyncsite.notiservice.domain.model.NotificationChannel.ChannelType channelType,
                                                     String language,
                                                     Boolean isActive,
                                                     int page,
                                                     int size);
}
