package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NotificationTemplateResponse {

    private final String templateId;
    private final String eventType;
    private final String channelType;
    private final String language;
    private final String titleTemplate;
    private final String contentTemplate;
    private final List<String> variables;
    private final boolean active;
    private final Integer version;
    private final String createdBy;
    private final String updatedBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static NotificationTemplateResponse from(NotificationTemplate template) {
        return NotificationTemplateResponse.builder()
                .templateId(template.getTemplateId())
                .eventType(template.getEventType())
                .channelType(template.getChannelType().name())
                .language(template.getLanguage())
                .titleTemplate(template.getTitleTemplate())
                .contentTemplate(template.getContentTemplate())
                .variables(template.getVariables() != null ?
                        template.getVariables().keySet().stream().toList() : List.of())
                .active(template.isActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
