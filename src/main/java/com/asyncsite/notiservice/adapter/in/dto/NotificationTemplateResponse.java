package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationTemplateResponse(
    String templateId,
    String eventType,
    String channelType,
    String language,
    String titleTemplate,
    String contentTemplate,
    List<String> variables,
    boolean active,
    Integer version,
    String createdBy,
    String updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static NotificationTemplateResponse from(NotificationTemplate template) {
        return new NotificationTemplateResponse(
                template.getTemplateId(),
                template.getEventType(),
                template.getChannelType().name(),
                template.getLanguage(),
                template.getTitleTemplate(),
                template.getContentTemplate(),
                template.getVariables() != null ?
                        template.getVariables().keySet().stream().toList() : List.of(),
                template.isActive(),
                template.getVersion(),
                null, // createdBy
                null, // updatedBy
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
