package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationTemplateResponse(
        String templateId,
        ChannelType channelType,
        EventType eventType,
        String titleTemplate,
        String contentTemplate,
        Map<String, String> variables,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NotificationTemplateResponse from(NotificationTemplate template) {
        return new NotificationTemplateResponse(
                template.getTemplateId(),
                template.getChannelType(),
                template.getEventType(),
                template.getTitleTemplate(),
                template.getContentTemplate(),
                template.getVariables(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
