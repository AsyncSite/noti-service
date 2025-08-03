package com.asyncsite.notiservice.adapter.out.persistence.entity;

import com.asyncsite.notiservice.common.JsonUtil;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "notification_templates")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String templateId;
    @Version
    private Integer version;
    private ChannelType channelType;
    private EventType eventType;
    @Column(columnDefinition = "TEXT")
    private String titleTemplate;
    @Column(columnDefinition = "TEXT")
    private String contentTemplate;
    @Column(columnDefinition = "JSON")
    private String variables;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static NotificationTemplateEntity from(NotificationTemplate template) {
        return NotificationTemplateEntity.builder()
                .templateId(template.getTemplateId())
                .version(template.getVersion())
                .channelType(template.getChannelType())
                .eventType(template.getEventType())
                .titleTemplate(template.getTitleTemplate())
                .contentTemplate(template.getContentTemplate())
                .variables(Objects.isNull(template.getVariables()) ? null : JsonUtil.toJson(template.getVariables()))
                .active(template.isActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    public NotificationTemplate toDomain() {
        return NotificationTemplate.builder()
                .templateId(templateId)
                .version(version)
                .channelType(channelType)
                .eventType(eventType)
                .titleTemplate(titleTemplate)
                .contentTemplate(contentTemplate)
                .variables(Objects.isNull(this.variables) ? null : (Map<String, String>) JsonUtil.fromJson(variables, Map.class))
                .active(active)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
