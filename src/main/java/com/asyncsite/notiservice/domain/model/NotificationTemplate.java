package com.asyncsite.notiservice.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder(toBuilder = true)
public class NotificationTemplate {
    private String templateId;
    private String eventType;
    private NotificationChannel.ChannelType channelType;
    private String language;
    private String titleTemplate;
    private String contentTemplate;
    private Map<String, String> variables;
    private boolean active;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String renderTitle(Map<String, Object> data) {
        return renderTemplate(titleTemplate, data);
    }

    public String renderContent(Map<String, Object> data) {
        return renderTemplate(contentTemplate, data);
    }

    private String renderTemplate(String template, Map<String, Object> data) {
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    public boolean isForEventType(String eventType) {
        return this.eventType.equals(eventType);
    }

    public boolean isForChannelType(NotificationChannel.ChannelType channelType) {
        return this.channelType == channelType;
    }

    public boolean isForLanguage(String language) {
        return this.language.equals(language);
    }
}
