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
    private String templateId;
    @Version
    private Integer version;
    private ChannelType channelType;
    private EventType eventType;
    @Column(columnDefinition = "TEXT")
    private String titleTemplate;
    @Column(columnDefinition = "TEXT")
    private String contentTemplate;
    @Column(columnDefinition = "TEXT")
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
        Map<String, String> parsedVariables = null;
        if (this.variables != null && !this.variables.isEmpty()) {
            try {
                // HTML 디코딩 처리 (만약 HTML 인코딩된 경우)
                String cleanJson = this.variables.replace("&quot;", "\"");
                parsedVariables = (Map<String, String>) JsonUtil.fromJson(cleanJson, Map.class);
            } catch (Exception e) {
                // 파싱 실패 시 로그 남기고 null 반환
                e.printStackTrace();
            }
        }
        
        return NotificationTemplate.builder()
                .templateId(templateId)
                .version(version)
                .channelType(channelType)
                .eventType(eventType)
                .titleTemplate(titleTemplate)
                .contentTemplate(contentTemplate)
                .variables(parsedVariables)
                .active(active)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
