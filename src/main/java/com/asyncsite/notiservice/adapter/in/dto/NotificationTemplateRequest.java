package com.asyncsite.notiservice.adapter.in.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NotificationTemplateRequest {

    private String eventType;
    private String channelType;
    private String language;
    private String titleTemplate;
    private String contentTemplate;
    private List<String> variables;
    private boolean isActive = true;
    private Map<String, Object> previewVariables;

    public com.asyncsite.notiservice.domain.model.NotificationTemplate toDomain() {
        return com.asyncsite.notiservice.domain.model.NotificationTemplate.builder()
                .eventType(this.eventType)
                .channelType(com.asyncsite.notiservice.domain.model.NotificationChannel.ChannelType.valueOf(this.channelType))
                .language(this.language)
                .titleTemplate(this.titleTemplate)
                .contentTemplate(this.contentTemplate)
                .variables(this.variables != null ? this.variables.stream().collect(java.util.stream.Collectors.toMap(v -> v, v -> "")) : java.util.Map.of())
                .active(this.isActive)
                .build();
    }
}
